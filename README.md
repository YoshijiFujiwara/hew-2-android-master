
# hew-2-android-master

Gathering イベント幹事支援アプリ

## Git開発の注意点

**<span style="font-size: 200%">開発始める前に必ずpull!!</span>**  
作業にひと段落ついたらこまめにpull,commit,pushしましょう。  
コミットコメントは変更したことを分かりやすく書いておきましょう。  
書くこと多すぎる場合コミットしなさすぎかと思います。よくやらかします。  
[どうしようもなくなった時](http://www-creators.com/archives/1097)  
ブランチはdevelopで開発していってください。（git-flowなので分かる方はそれで）

## フロントエンド開発の注意点

### layout.xml

Constraintレイアウトで書いてください。  
命名はsnake_case  
Activityで使うものは activity_アクティビティ名.xml  
Fragmentで使うものは fragment_フラグメント名.xml  
それ以外は分かりやすくて被らなければOKです。  
出来ればサブフォルダで管理していきたいのですが、方法が分かっていないので後日構成に変更があるかもしれません。  

### リソースxml

文字列や色などxmlファイルで管理されているものに関しては適切なものがなければ値を追加していって下さい。  
おそらく追加していないと黄色の警告マークが出るので見つけたら潰しましょう。  
色に関してはデザインの統一性に影響してくるので出来るだけ既存のものを使いましょう。  
アイコンとかも足りないと思うので無ければ追加で。  

### Activity/Fragment/Adapter/View

大抵の画面はFragmentで書いた方が安全だと思いますが、動作に問題がないならどちらでもいいです。  
（Activityで書くとFragment書かないといけなくなった時が面倒だけど１画面だけなのにActivity/Fragmentってのも微妙・・・）  
サイドバーを共有したい画面に関してはFragment強制です。  
命名は先頭大文字のCamelCase。  
Activityは XxxxxxActivity でActivitiesフォルダ内に作成。  
Fragmentは XxxxxxFragment でFragmentsフォルダ内に作成。  
独自Viewは XxxxxxView でviewsフォルダ内に作成。  
Adapterは Xxxxxxadapters でviews/adaptersフォルダ内に作成。  
AndroidStudioで普通に作れば大丈夫だと思います。  

### その他クラス追加/API関連

その他のクラスも命名は先頭大文字のCamelCase。  
ApiService内のURLは最終的にリファレンスに乗ってる分を全て書くことになるので使う分はどんどん追加していってください。  
API用でプロパティのみのクラスを作成する場合は apiフォルダ内に作成してください。  

## gitコマンド

### 作業前

```shell
git pull origin develop
git checkout feature/<自分のブランチ名>
git merge develop
```

### ブランチ

<自分のブランチ名>は適当につけてください。かっこはいらない。  

```shell
git branch feature/<自分のブランチ名>
git checkout feature/<自分のブランチ名>
```  
  
作業後：１〜２時間に一回はしましょう  

```shell
git add .
git commit -m "コメント"
(ブランチ作成後初回)git push -u origin feature/<自分のブランチ名>
(２回目以降)git push
```
  
### マージ

作業がひと段落したらorその日の作業終わりに  
動作確認をしてからマージしてください！

```shell
git add .
git commit -m "developをマージする前にとりあえずコミット"
git checkout develop
git pull
git checkout feature/<自分のブランチ名>
git merge develop
```

### コンフリクトした場合

競合箇所を修正  

```shell
git status
```

で競合しているファイルが確認できるので、対象ファイルを開いて以下のような部分を見つける。

```
<<<<<<< HEAD
自分の環境の変更点
=======
マージを試みた他の環境での変更点
>>>>>>> [commit id]
```

変更点を確認して、両方残す・どちらかだけ残す・再編集してより良い形に書き換えるなどして競合を解消する。  
  
その後、feature/<自分のブランチ名>ブランチで  

```shell
git add .
git commit -m "コメント"
git push
```

github上からプルリクエストを作ってdevelopとマージ  

## バックエンドメモ

### プレゼン用データ
id 1~4を、プレゼン用データとして追加しました。(テスト太郎を始めとするテストデータは、依然として残しております。)  
パスワードは全員「hoisulu」  
幹事役 １人（id: 1）
```
松坂 一穂
matsu@hew.com
```
先生役 ２人(id: 2, 3)
```
山田　忠明
yamada@hew.com
```
```
田中　信也
tanaka@hew.com
```
学生役 1人(id: 4)
```
藤原 吉司
fuji@hew.com
```
 
### デモ機用データ
id 5~7を、プレゼン用データとして追加しました。(テスト太郎を始めとするテストデータは、依然として残しております。)  
パスワードは全員「hoisulu」  
幹事役 １人（id: 5）
```
浜田　太郎(デモ)
demo5@hew.com
```
先生役 ２人(id: 6, 7)
```
山田　忠明(デモ)
demo6@hew.com
```
```
田中　信也(デモ)
demo7@hew.com
```  
  
### 開発用テストユーザー(一時的に無効!!!!!!!!!!!!!!)
```
testuser@example.com  
hoisulu 
```
### 開発用テストユーザー(一時的に無効!!!!!!!!!!!!!!!。プロフィールを変更するなど、サブで使用する用)
```
testuser48@example.com  
hoisulu  
```
```
testuser49@example.com  
hoisulu  
```
```
testuser50@example.com  
hoisulu  
```  

### 管理者画面

<https://hew-2-admin-panel-nuxt.now.sh/>
