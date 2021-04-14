# SAML勉強メモ+XSW

## Overview

### 既存問題

ビジネス向けアプリでは、社内環境などで構築することが多い。  
複数のアプリをユーザ情報を共有したい場合に、Microsoft Active Directoryなどを利用して共有と管理しています。

しかし、今の時代になると、クラウドサービスなどの連携がだんだん必要になってくるので、  
社内環境という概念がだんだんなくなり、じゃあユーザ情報をどうやって外部のクラウドサービスと連携するのかが問題になっています。

### ユーザに対する不便

ユーザは一つのアプリのみを利用する場合に、そのままアプリにユーザ登録してログインすればいいですが、  
ユーザは複数のアプリを利用したい場合に、各アプリのアカウント情報を管理しないといけない。  
もちろん同じユーザネームとパスワードならいいんだけど、
各アプリのパスワードに対するポリシーもそもそも違うので、  
設定したパスワードがばらばらの場合にもっとメンテナンスしにくくなります。

もしあるアプリがユーザの全てのアカウントを一括管理してくれるとうれしいという発想からSAMLが誕生した。

### 用語

SAMLは登場したので、SAMLが使っている用語を先に説明しないと、後で苦労するので先に用語を説明する。

- Service Provider(SP)：サービスを提供しているひと、基本的にユーザが利用しているアプリ
- Identity Provider(IdP)：ユーザ情報を管理している
- SAML Request：SPが認証するときのリクエスト
- SAML Response：IdPがリクエストに対して返した認証レスポンス、ユーザの認証情報を含んでいる
- SP-initiated：SPから認証が始まる。通常ではユーザが認証なしの状態でSPを直接アクセスするとかの場合
- IdP-initiated：IdPから認証が始まる。通常ではユーザはIdPにまず認証を行い、IdPからまた各SPに行くの場合

#### 注意

1. SPとIdPは直接通信しないこと、ブラウザを介していろいろやり取りしている
2. SPはどのIdPとやり取りするかを事前に知るべき
3. SPはIdPからのSAML assertionを貰った時点ではじめてユーザの情報を知ることができる
4. SPとIdPはどっちからスタート可能
5. SAML認証は非同期。SPはリクエストの情報とかを保持しないのでレスポンスは全ての情報を含むべき。

### メリット

1. アプリのビジネスロジックと認証ロジックを分離させることができる
2. アプリ側は認証を管理する必要がなくなり、認証専門家に任せて安全性の向上
3. ユーザはマルチアプリを利用している場合のエクスペリエンスの向上

## SAMLとは？

SAML (Security Assertion Markup Language)。

この名称だけを見ると、これは言語だとわかると思いますが、じゃあなぜSSOとかの話になるでしょう？

### Markup Language

他のML系の言語で言えば、`XML`とか`HTML`とかは出てくると思います。

`SAML`というのは`XML`の中の一種である。  
wiki：<https://en.wikipedia.org/wiki/List_of_XML_markup_languages>

つまり、`SAML`というものは本質的に言うと`XML`形式で定義された言語です。

### Assertion

Assertionとは何かを表明するの意味です。  
例えば、

- AさんはAdminです
- Bさんは一般ユーザです
- CさんはグループC1に属しています

とかは全部アサーションだと思ってもらえるといいと思います。

つまり。`SAML`というものはユーザの情報に関するアサーションです。  
IdPは自分で表現したいユーザの情報を`SAML`という形でSPへ渡す。

### Security

SPはIdPから貰ったアサーション（SAML）を解析し、その中のユーザ情報を利用してユーザの身分を確認しますが、  
改ざんなどを防止するために、Securityという概念が出てきました。

つまり、任意のSAMLを無条件に信用しているわけではなく、  
セキュリティをチェックした上で利用するの意味になります。

## SAMLの構造

一番基本的なものは**Assersion**です。
Assertionはユーザの情報を含んでいます。

Assertionはある**Protocol**に従ってReceiverとResponserの間でデータ通信しています。  
例えば一番わかりやすいのは**Authentication Request Protocol**、`<AuthnRequest>`を使ってAssersionを取得する。  
Web Browser SSOではよく使われています。

SAML ProtocolはSAML内部で定義したプロトコルで、
実際にネットワーク上で通信を行うためにどのトランスポートプロトコルを利用するかを設定する必要があります。  
基本的にはSOAPとHTTP二種があります。  
この設定自体はSAML内では**Bindings**と称しています。

最終的に**Profile**という形でSAMLの構造が作られます。

一つのProfileはAssersion、Protocol、Bindingsを含んでいます。
また、ユースケースが異なることによってProfileの中身も違ってきます。

## Web Browser SSO Profile

今の仕事ではWeb Browser SSOと関わっているので、それを細かくキャッチアップしています。

でもWeb Browser SSOのユースケース中でもいろんな種類があるのでそれを今回まとめました。  
一部Artifactに関する内容を割愛。

### SP initiated: POST -> POST binding

SP initiatedは前文述べたSPからの認証です。

ユーザは先にSPにアクセスします。  
SPはユーザのログオンセッションを持っていないので（まだログインしていない）、  
SPは`<AuthnRequest>`を作成し、ブラウザを介して**POST**方式でIdPへリクエストする、  
IdPはユーザとの認証を確認後に`<Response>`を作成し、ブラウザを介して**POST**方式でSPへリクエストする。  
SPはResponseの情報を確認し、Userのログイン処理とかを行って、UserはSPが提供しているリソースをアクセスする。

### SP initiated: Redirect -> POST binding

ユーザは先にSPにアクセスします。  
SPはユーザのログオンセッションを持っていないので（まだログインしていない）、  
SPは`<AuthnRequest>`を作成し、ブラウザを介してIdPへリダイレクトする、  
IdPはユーザと認証を確認後に`<Response>`を作成し、ブラウザを介して**POST**方式でSPへリクエストする。  
SPはResponseの情報を確認し、Userのログイン処理とかを行って、UserはSPが提供しているリソースをアクセスする。

### IdP initiated: POST binding

ユーザは先にIdPにアクセスします。  
IdPにログインし、次にアクセスしたいリソースを選択する。  
IdPは`<Response>`を作成し、ブラウザを介して**POST**方式でSPへリクエストする。  
SPはResponseの情報を確認し、ユーザログイン用のセッションを作成してUserにリソースを提供する。

## XSW

これから本題に入りますが、  
XSW（XML Signature Wrapping Attacks）といいます。
SSO VeriicatorとSSO Processorの分離を悪用した攻撃です。

IdPから戻された`<Response>`を攻撃者により改ざんし、SPは改ざんされた`<Response>`を解析し、
攻撃者の意図通りに別のユーザでログインできてしまう恐れがあります。

### SAMLResponse

```xml
<?xml version="1.0" encoding="UTF-8"?>
<samlp:Response ... ID="_df55c0bb940c687810b436395cf81760bb2e6a92f2" ...>
  <saml:Issuer>...</saml:Issuer>
  <ds:Signature ...>
    <ds:SignedInfo>
      <ds:CanonicalizationMethod .../>
      <ds:SignatureMethod .../>
      <ds:Reference URI="#_df55c0bb940c687810b436395cf81760bb2e6a92f2">...</ds:Reference>
    </ds:SignedInfo>
    <ds:SignatureValue>...</ds:SignatureValue>
    <ds:KeyInfo>...</ds:KeyInfo>
  </ds:Signature>
  <samlp:Status>...</samlp:Status>
  <saml:Assertion ...>
    <saml:Issuer>...</saml:Issuer>
    <ds:Signature ... >...</ds:Signature>
    <saml:Subject>
      <saml:NameID ...>...</saml:NameID>
      <saml:SubjectConfirmation ...>
        <saml:SubjectConfirmationData .../>
      </saml:SubjectConfirmation>
    </saml:Subject>
    <saml:Conditions ...>...</saml:Conditions>
    <saml:AuthnStatement ...>...</saml:AuthnStatement>
    <saml:AttributeStatement>...</saml:AttributeStatement>
  </saml:Assertion>
</samlp:Response>
```

これはIdPから返された一般的な`<Response>`になります。

### SPのResponseの解析フロー

上記の`<Response>`の例で言いますと、ルートにResponseがあって、
その直下に`<Signature>`と`<Assertion>`があります。

`<Signature>`はValidate用で、`<Assertion>`は具体的なユーザ情報を含んでいます。

通常の解析フローでは、

1. Signatureのバリデーション
   1. Signature > SignedInfo > Referenceを確認する
   2. ReferenceのURIを確認し、それに関連するエレメントを特定する
   3. エレメントのバリデートを行う
2. Assertionから必要なデータ取得、**Top-Down方式で上から下までAssersionを特定する**

この二つのステップがあり、攻撃者は**Top-Down方式で上から下までAssersionを特定する**を狙って攻撃できます。

つまりバリデーションはそのまま元のSignatureを利用して、
Assersionの部分を改ざんして、元のAssersionを別のところに移動させたりすることで、
バリデーションは正常に通るんですが、
Top-Down方式で改ざんされたAssersionが取得されてしまうという脆弱性です。

全部八つの種類があるので、それぞれ説明します。

### XSW #1

```xml:改ざん前
<Response ID="foo">
  <Signature>
    <SignedInfo>
      <Reference URI="foo">
    </SignedInfo>
  </Signature>
  <Assertion ID="bar">
    <Subject>
  <Assertion>
</Response>
```

```diff:改ざん後
+ <Response ID="evil">
- <Response ID="foo">
    <Signature>
      <SignedInfo>
        <Reference URI="foo">
      </SignedInfo>
+     <Response ID="foo">
+       <Assertion ID="bar">
+         <Subject>
+       <Assertion>
+     </Response>
    </Signature>
-   <Assertion ID="bar">
-     <Subject>
-   <Assertion>
+   <Assertion ID="evil">
+     <Subject>
+   <Assertion>
  </Response>
```

Responseと直下のAssersionをSignature以下に移動させ、
改ざんされたAssersionをResponse直下に置く。

### XSW #2

```xml:改ざん前
<Response ID="foo">
  <Signature>
    <SignedInfo>
      <Reference URI="foo">
    </SignedInfo>
  </Signature>
  <Assertion ID="bar">
    <Subject>
  <Assertion>
</Response>
```

```diff:改ざん後
+ <Response ID="evil">
- <Response ID="foo">
+   <Response ID="foo">
+     <Assertion ID="bar">
+       <Subject>
+     <Assertion>
+   </Response>
    <Signature>
      <SignedInfo>
        <Reference URI="foo">
      </SignedInfo>
    </Signature>
-   <Assertion ID="bar">
-     <Subject>
-   <Assertion>
+   <Assertion ID="evil">
+     <Subject>
+   <Assertion>
  </Response>
```

**XSW #1**と似ていますが、
元のResponseとAssersionをSignatureと同じ階層で新しいResponseの直下に移動させ、
新しいResponse直下に改ざんされたAssersionを置く。

### XSW #3

**SignatureのReferenceはResponseではなくて、今回はAssertionになりました。**

```xml:改ざん前
<Response ID="bar">
  <Assertion ID="foo">
    <Subject>
    <Signature>
      <SignedInfo>
        <Reference URI="foo">
      </SignedInfo>
    </Signature>
  </Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Assertion ID="evil">
+     <Subject>
+   </Assertion>
    <Assertion ID="foo">
      <Subject>
      <Signature>
        <SignedInfo>
          <Reference URI="foo">
        </SignedInfo>
      </Signature>
    </Assertion>
  </Response>
```

Responseの直下に改ざんされたAssertionを**一つ目**の子供エレメントとして挿入します。

### XSW #4

```xml:改ざん前
<Response ID="bar">
  <Assertion ID="foo">
    <Subject>
    <Signature>
      <SignedInfo>
        <Reference URI="foo">
      </SignedInfo>
    </Signature>
  </Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Assertion ID="evil">
+     <Subject>
+     <Assertion ID="foo">
+       <Subject>
+       <Signature>
+         <SignedInfo>
+           <Reference URI="foo">
+         </SignedInfo>
+       </Signature>
+     </Assertion>
+   </Assertion>
-  <Assertion ID="foo">
-    <Subject>
-    <Signature>
-      <SignedInfo>
-        <Reference URI="foo">
-      </SignedInfo>
-    </Signature>
-  <Assertion>
  </Response>
```

**XSW #3**とちょっと似ているのですが、
まず改ざんされたAssertionを作ってResponse直下において、
元のAssertionを改ざんされたAssertionの下に置きます。

### XSW #5

**AssertionとSignatureは親子関係を持っていない場合。**

```xml:改ざん前
<Response ID="bar">
  <Signature>
    <SignedInfo>
      <Reference URI="foo">
    </SignedInfo>
  </Signature>
  <Assertion ID="foo">
    <Subject>
  <Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Assertion ID="evil>
+     <Subject>
+     <Signature>
+       <SignedInfo>
+         <Reference URI="foo">
+       </SignedInfo>
+     </Signature>
+   </Assertion>
-   <Signature>
-     <SignedInfo>
-       <Reference URI="foo">
-     </SignedInfo>
-   </Signature>
    <Assertion ID="foo">
      <Subject>
    <Assertion>
  </Response>
```

改ざんされたAssertionを作成して、元のSignatureを改ざんされたAssersionの直下に移動させる。

### XSW #6

```xml:改ざん前
<Response ID="bar">
  <Signature>
    <SignedInfo>
      <Reference URI="foo">
    </SignedInfo>
  </Signature>
  <Assertion ID="foo">
    <Subject>
  <Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Assertion ID="evil">
+     <Subject>
+     <Signature>
+       <SignedInfo>
+         <Reference URI="foo">
+       </SignedInfo>
+       <Assertion ID="foo">
+         <Subject>
+       <Assertion>
+     </Signature>
+   </Assertion>
-   <Signature>
-     <SignedInfo>
-       <Reference URI="foo">
-     </SignedInfo>
-   </Signature>
-   <Assertion ID="foo">
-     <Subject>
-   <Assertion>
  </Response>
```

**XSW #5**と似ているのですが、
改ざんされたAssertionを作成し、Response直下に置いて、
元のSignatureを改ざんされたAssersionの直下に移動させる。
また、さらに元のAssersionを元のSignatureの直下に移動させる。

### XSW #7

```xml:改ざん前
<Response ID="bar">
  <Assertion ID="foo">
    <Subject>
    <Signature>
      <SignedInfo>
        <Reference URI="foo">
      </SignedInfo>
    </Signature>
  </Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Extensions>
+     <Assertion ID="evil">
+       <Subject>
+     </Assertion>
+   </Extensions>
    <Assertion ID="foo">
      <Subject>
      <Signature>
        <SignedInfo>
          <Reference URI="foo">
        </SignedInfo>
      </Signature>
    </Assertion>
  </Response>
```

`<Extensions>`は実に有効なエレメントの一種です。また制限も少ないらしいです。
上記のようにResponse直下にの**一つ目の子供**として、`<Extensions>`をまた直下の改ざんされたAssertionをとともに置きます。

### XSW #8

```xml:改ざん前
<Response ID="bar">
  <Signature>
    <SignedInfo>
      <Reference URI="foo">
    </SignedInfo>
  </Signature>
  <Assertion ID="foo">
    <Subject>
  <Assertion>
</Response>
```

```diff:改ざん後
  <Response ID="bar">
+   <Assertion>
+     <Subject>
+     <Signature>
+       <SignedInfo>
+         <Reference URI="foo">
+       </SignedInfo>
+       <Object>
+         <Assertion ID="foo">
+           <Subject>
+         </Assertion>
+       </Object>
+     </Signature>
+   </Assertion>
-   <Signature>
-     <SignedInfo>
-       <Reference URI="foo">
-     </SignedInfo>
-   </Signature>
-   <Assertion ID="foo">
-     <Subject>
-   <Assertion>
  </Response>
```

**XSW #7**と似ているのですが、
`<Object>`という制限の少ないエレメントを利用して改ざんを行います。
改ざんされたAssertionをResponse直下に作成し、
元のAssertionを`<Object>`にラップして元のSignatureの直下に置きます。

## 最後

これは全てのXSW攻撃のパターンになります。
これを知った上で丁寧に実装すればこの攻撃を防止できるのではないかと思います。

## 参考

- <http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html>
- <https://developer.okta.com/docs/concepts/saml/>
- <https://www.oasis-open.org/committees/download.php/11511/sstc-saml-tech-overview-2.0-draft-03.pdf>
- <http://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf>
- <https://medium.com/brightlab-techblog/implement-single-sign-on-saml-strategy-with-node-js-passport-js-e8b01ff79cc3>
- <https://at-virtual.net/%E3%82%BB%E3%82%AD%E3%83%A5%E3%83%AA%E3%83%86%E3%82%A3/saml%E8%AA%8D%E8%A8%BC%E3%81%AE%E8%84%86%E5%BC%B1%E6%80%A7%E3%81%A8%E5%AF%BE%E7%AD%96/>
- <https://epi052.gitlab.io/notes-to-self/blog/2019-03-13-how-to-test-saml-a-methodology-part-two/#xml-signature-wrapping>