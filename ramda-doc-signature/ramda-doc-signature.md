# 前書き

今取り込んでいるタスクにはRamdaを使おうとしているので、一通りRamdaのDocを読みました。  
特に、Ramdaが提供している各関数は実際に何をしているかを理解を深めるために、  
RamdaのType Signatureを理解する必要性を感じているので、  
ここで軽く自分の理解をまとめてみます。

## Arrow

```js
* → Boolean
```

上記のようにRamdaのType Signatureの書き方があります。  
実はこれが[not](<https://ramda.cn/docs/#not>)のType Signatureです。

Arrowは何かというと、左側は引数で、右側は帰り値で良いでしょう。

つまり、上記のnotの関数で言えば、任意の値を入力値（引数）として入れて、Booleanを返すということがわかります。

## Type

### 一般的な

先ほどの`*`はAny、任意の意味ですが、他にいろいろあります。

> ちなみに、Any / `*`はこれから非推奨になる予定らしい。

- Number
- Boolean
- String
- Object
- Type：JSで定義しているType
- [*], [a]：リスト
- {*}、{k: v}：オブジェクト
- (xxx -> yyy)：Function、関数

### ちょっと特殊な

- Int
- Promise
- [a -> b]：関数をリストの全ての項目に適用する
- [[]]：2-dリスト
- *...：任意個数
- Ord：

## 資料

- <https://ramdajs.com/docs/>
- <https://github.com/ramda/ramda/wiki/Type-Signatures>