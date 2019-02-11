
# hew-2-android-master

Gathering イベント幹事支援アプリ

## Git開発の注意点

開発始める前に必ずpull!!
作業にひと段落ついたらこまめにpull,commit,pushしましょう。  
コミットコメントは変更したことを分かりやすく書いておきましょう。
書くこと多すぎる場合コミットしなさすぎかと思います。よくやらかします。
どうしようもなくなった時<http://www-creators.com/archives/1097>  
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

## バックエンドメモ

### テストユーザー

testuser@example.com  
secret  

### APIURL

<https://laravel-dot-eventer-1543384121468.appspot.com/>

### phpmyadmin

<https://eventer-1543384121468.appspot.com>

### APIリファレンス

<https://laravel-dot-eventer-1543384121468.appspot.com/docs/index.html>