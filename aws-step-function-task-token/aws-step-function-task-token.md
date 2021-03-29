## 背景

既存のシステムではバッチジョブ実行機能があり、
しかし、バッチ実行の時間がかかるので（長い場合に数時間がかかる）、
バッチ実行をAWS-Lambdaで管理することができない。

## ジョブ実行ステータスをポーリング？

まずAWS-Lambdaでジョブをキックします。
ジョブが完了までに数秒ごとにポーリングしてステータスを確認します。

これは実現可能ですが、
無駄の実行がたくさんあるので今回のケースは向いてないと思います。

## 今回の提案

### 実際のバッチ実行ロジックをモック

AWS-Lambdaを作成します。

```js
async function sleep(t) {
    return await new Promise(r => {
        setTimeout(() => {
            r();
        }, t);
    });
}

exports.handler = async (event) => {
    // TODO implement
    const response = {
        statusCode: 200,
        body: JSON.stringify('Success!'),
    };
    console.log(`Start ${new Date()}`);
    console.log(event);
    
    await sleep(5000); // mock some process
    
    console.log(`End ${new Date()}`);
    
    return response;
};
```

これでバッチ実行は5秒かかるというモックになります。

**NOTE：**
実際のバッチはLambdaではないですが、今回はデモのためこういう風に設定しました。

### バッチ実行用のトリガーを作成する

```js
const AWS = require('aws-sdk');
var lambda = new AWS.Lambda();

exports.handler = async(event) => {
    // TODO implement
    const response = {
        statusCode: 200,
        body: event,
    };
    console.log(`Start ${new Date()}`);
    console.log(event);

    var params = {
        FunctionName: "arn:aws:lambda:ap-northeast-1:1234567890123:function:mock-batch",
        Payload: JSON.stringify({}),
        Qualifier: "mock-batch"
    };
    const result = await lambda.invoke(params, function(err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else console.log(data); // successful response
    }).promise();

    console.log(result);
    console.log(`End ${new Date()}`);

    return response;
};
```

このLambdaを実行すると、予め設定したバッチが実行されます。
