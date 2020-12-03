最近統計学に関する本を読んでいます。  
その中に[モンティ・ホール問題](https://ja.wikipedia.org/wiki/%E3%83%A2%E3%83%B3%E3%83%86%E3%82%A3%E3%83%BB%E3%83%9B%E3%83%BC%E3%83%AB%E5%95%8F%E9%A1%8C)という問題を紹介しています。

ちょっと直感に乖離しているの問題なので、プログラミングで実験してみよと思いました。
そのまま書くのもつまらないので、
[FunctionalInterface](https://docs.oracle.com/javase/jp/8/docs/api/java/lang/FunctionalInterface.html)や[Strategy Pattern](https://en.wikipedia.org/wiki/Strategy_pattern)を使って遊びました。



## 1 問題について軽く

そのままウィキからコピーします。

- (1) 3つのドア (A, B, C) に（景品、ヤギ、ヤギ）がランダムに入っている。

- (2) プレーヤーはドアを1つ選ぶ。

- (3) モンティは残りのドアのうち1つを必ず開ける。

- (4) モンティの開けるドアは、必ずヤギの入っているドアである。
- (5) モンティはプレーヤーにドアを選びなおしてよいと必ず言う。



## 2 コードを書く

### 2.1 Main

まず最終的に実行するMainクラスの部分を書きます。

```java
public class Main {

    public static void main(String[] args) {
        // 最後に書く
    }

    private static boolean play(ChooseStrategy strategy) {
        Player player = new Player(strategy);
        Host host = new Host();
        Stage stage = new Stage();

        // Player make his first choice
        stage.doOperation(player::chooseDoor);
        // System.out.println(stage);

        // Host open a door
        stage.doOperation(host::openDoor);
        // System.out.println(stage);

        // Player make his second choice
        stage.doOperation(player::chooseAgain);
        // System.out.println(stage);

        return stage.isCorrect();
    }
}
```

まず`play`メソッドを見ていただくと、いくつかのことを行いました。

- プレーヤーを生成します。（ストラテジーを指定します、後で紹介）
- モンティを生成します。（ホスト）
- 三つドアを持っているステージを生成します。



次にゲームのルールに従ってそのまま実行手順を書いてみました。

- プレーヤーはステージに対してドアを選択しました。
- ホストはステージに対して一つのドアを開けました。
- プレーヤーは二回目（選びなおし）の選択を行いました。
- ステージを確認してプレーヤーの選択は当たってるかを返しました。



ストラクチャーは大体できたので、各部分を実装します。

### 2.2 ChooseStrategy

一回目のチョイスはランダムなのでdefaultでロジックを書きます。
二回目はストラテジーにより異なるので継承先に実装を任せます。

```java
/**
* 一回目と二回目の選択ストラテジー
*/
@FunctionalInterface
public interface ChooseStrategy {

    Random random = new Random();

    default void chooseFirstTime(List<Door> doors) {
        doors.get(random.nextInt(doors.size())).setChosen(true);
    }

    void chooseSecondTime(List<Door> doors);
}
```

#### 2.2.0 Door

上記のストラテジーには`Door`クラスが出ていたのでその中身を紹介します。

各ドアの属性は以下の通りです。
ストラテジーはドアリストに対して操作します。

```java
@AllArgsConstructor
@Data
public class Door {

    int index;
    // is or not has the price
    boolean isAnswer;
    // is or not opened by host
    boolean isOpened;
    // is or not chosen by player
    boolean isChosen;

}
```



#### 2.2.1 NotChangeChoiceStrategy

ここはちょっと面白いところですが、
もしプレーヤーは一回目の選択を変えないと決めた場合に何もしなくていいので、
ここも何も実装しなくて大丈夫です。

```java
public class NotChangeChoiceStrategy implements ChooseStrategy {

    @Override
    public void chooseSecondTime(List<Door> doors) {
        // do nothing
    }
}
```

#### 2.2.2 ChangeChoiceStrategy

もし選択を変えると決めた場合に、
残りの二つのドアの状態を変えます。

まず三つのドアの中に一つのドアが開けられたので除外して、
次に残りの二つのドアの選択状態を切り替えます。

```java
public class ChangeChoiceStrategy implements ChooseStrategy {

    @Override
    public void chooseSecondTime(List<Door> doors) {
        doors.stream().filter(d -> !d.isOpened()).forEach(d -> d.setChosen(!d.isChosen()));
    }
}
```

ここまででプレーヤーはどういう方法でドアを選択するかは定義できました。
次にプレーヤー自体を定義します。

### 2.3 Player

めちゃくちゃ簡単ですが、
一回目と二回目の動きは全部ストラテジーにより行います。

```java
public class Player {

    private ChooseStrategy strategy;

    public Player(ChooseStrategy strategy) {
        this.strategy = strategy;
    }

    public void chooseDoor(List<Door> doors) {
        strategy.chooseFirstTime(doors);
    }

    public void chooseAgain(List<Door> doors) {
        strategy.chooseSecondTime(doors);
    }
}
```

### 2.4 Host

ホストは下記コードのようにまず開けるドアを確認してその中にランダムで一つをピックアップして開けます。

```java
public class Host {

    /**
     * Host will open the door which does not have the price.
     * @param doors doors in stage
     */
    public void openDoor(List<Door> doors) {
        doors = doors.stream().filter(this::canOpen).collect(Collectors.toList());
        Collections.shuffle(doors);
        doors.get(0).setOpened(true);
    }

    // if a door is not the answer nor the chosen one, then can be opened.
    private boolean canOpen(Door door) {
        return !door.isAnswer() && !door.isChosen();
    }
}
```

### 2.5 Stage

まずステージのインスタンスが作られた時に、ステージ内の三つのドアをランダムで生成します。

注目してもらいたいのは`doOperation`です。
今回の考え方はステージはあくまでドアを状態を保存する場所で、
一切具体的なオペレーションが入ってないです。

なので、ステージはあくまで自分が持っている`doors`をステージに操作したい人（プレーヤー・ホスト）に渡します。
該当の人がステージの定義した操作基準によってステージのドアを操作します。

```java
public class Stage {

    private List<Door> doors;
    private final Random random = new Random();

    public Stage() {
        this.doors = randomInitDoors();
    }

    private List<Door> randomInitDoors() {
        int trueIndex = random.nextInt(3);
        doors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            doors.add(new Door(i, i == trueIndex, false, false));
        }
        return doors;
    }

    public void doOperation(StageOperation op) {
        op.doOperation(doors);
    }

    public boolean isCorrect() {
        return this.doors.stream().filter(Door::isAnswer).anyMatch(Door::isChosen);
    }

    @Override
    public String toString() {
        return "Stage{" +
                "doors=" + doors +
                '}';
    }
}
```

では、`StageOperation`についてみてみましょう。

### 2.5.1 StageOperation

先ほどの`ChooseStrategy`と似ているけど、
`ChooseStrategy`に定義された操作は継承先用の基準です。

ここで定義された操作は任意のステージを操作したいクラス用のものです。
つまり、ステージを操作したければこのメソッドを実装してくださいの意味です。

```
@FunctionalInterface
public interface StageOperation {
    void doOperation(List<Door> doors);
}
```

たぶんここまで行くとわかってると思いますが、
もう一度最初のMainクラスを見ますと：

- player::chooseDoor
- host::openDoor
- player::chooseAgain

は全部暗黙で`StageOperation::doOperation`を実現したメソッドです。

なぜ`doOperation`のパラムが`StageOperation`を求めているのに、全然違うメソッドを渡しても動作するの原因は、
メソッドのシグネチャが一緒からです。

### 2.6 Main again

>TIPS：ここにNotChangeChoiceStrategyを使ってなくて、
>ラムダ式を使ってました。
>
>こういうふうにわざわざ各ストラテジーを書かずに、
>使いたいときにラムダ式で直接定義するのも便利かもですね。

```java
public static void main(String[] args) {
    int count = 3000;
    int correct = 0;
    for (int i = 0; i < count; i++) {
        // we can also use in-line lambda to replace the implemented NotChangeChoiceStrategy
        // if (play(new NotChangeChoiceStrategy)) {
        if (play(doors -> {})) {
            correct++;
        }
    }
    System.out.printf("not change choice: %d / %d = %.2f%%%n", correct, count, correct * 1.0 / count);

    correct = 0;
    for (int i = 0; i < count; i++) {
        if (play(new ChangeChoiceStrategy())) {
            correct++;
        }
    }
    System.out.printf("change choice: %d / %d = %.2f%%%n", correct, count, correct * 1.0 / count);
}
```

ちなみに`ChangeChoiceStrategy`もラムダ式で書けるのでぜひやってみてください。

## 3 実行結果

```log
not change choice: 998 / 3000 = 0.33%
change choice: 1948 / 3000 = 0.65%
```

やはり選択を変えたほうがいいですね。