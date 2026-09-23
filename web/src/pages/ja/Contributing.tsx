import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="貢献方法"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="OpenDisplay Androidに戻る"
    >
      <p>
        貢献を検討してくださりありがとうございます。これは個人が平日夜や週末に細々と続けているプロジェクトです —
        返信までの時間は一定しませんが、バグ報告やプルリクエストは本当に歓迎しています。
      </p>

      <section>
        <h2>行動規範</h2>
        <p>
          このプロジェクトは<a href="code-of-conduct.html">Contributor Covenant</a>に従います。参加する際は
          これを守ることが期待されます。
        </p>
      </section>

      <section>
        <h2>バグ報告・機能提案</h2>
        <p>
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a>
          でバグ報告または機能リクエストのテンプレートを使ってissueを開いてください。対応をぐっと早くするポイント:
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>証拠。</strong> スクリーンショット、<code>adb logcat</code>の出力、または実際の挙動を示す
            コマンドとその出力。貼り付ける前にIPアドレスや個人情報はマスクしてください。
          </li>
          <li>バグの場合は<strong>端末とAndroidのバージョン</strong>。</li>
          <li>まず既存のissueを確認してください — すでに報告されているかもしれません。</li>
        </ul>
      </section>

      <section>
        <h2>開発環境のセットアップ</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # ビルド
./gradlew installDebug           # 接続中の端末/エミュレータにインストール
./gradlew testDebugUnitTest      # ユニットテストを実行
adb logcat -s OpenDisplay:*      # アプリのログ`}</code>
        </pre>
        <p>
          Android SDKのインストールと、それを指す<code>ANDROID_HOME</code>/<code>local.properties</code>
          の設定が必要です。ビルド・使用方法の全体は{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a>を参照してください。
        </p>
      </section>

      <section>
        <h2>コードスタイル</h2>
        <ul>
          <li>
            <strong>常に、本当に必要なことを解決する最もシンプルな方法を選ぶ。</strong> 投機的な抽象化、
            早すぎる最適化、「後のために」の未使用の柔軟性は避ける。
          </li>
          <li>
            <strong>コメント</strong>: コードの途中に散らばった<code>{'//'}</code>コメントは書かない。
            クラス・関数のドキュメントはKDocに。1行やブロックの自明でない理由は<code>RATIONALE.md</code>に書き、
            インラインには書かない。
          </li>
          <li>
            Kotlin + Jetpack Composeで、<code>app/src/main/java/</code>にある既存の構成に従う。
          </li>
        </ul>
      </section>

      <section>
        <h2>テストとカバレッジ</h2>
        <p>
          純粋なロジック（パーサー、プロトコルの定数、Androidフレームワークに依存しないクラス）は、
          <strong>行カバレッジ95%以上</strong>を維持するユニットテストが必要です（CIの{' '}
          <code>jacocoCoverageVerification</code>で検証）。UI、サービス、<code>MediaCodec</code>/ソケット/
          <code>NsdManager</code>に依存するものはこの計算から除外されます。
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>コミットメッセージ</h2>
        <p>
          チェンジログを生成するのではなく、人に変更内容を説明するつもりで書いてください — <em>何を</em>だけでなく
          <em>なぜ</em>を書く。中身のない<code>chore: ...</code>のような汎用的な接頭辞は避ける。
        </p>
      </section>

      <section>
        <h2>プルリクエスト</h2>
        <ol>
          <li><code>main</code>からブランチを作成する。</li>
          <li>変更は絞り込む — 1つのPRに1つの内容の方がレビュー・マージしやすい。</li>
          <li>
            <code>./gradlew assembleDebug</code>と<code>./gradlew testDebugUnitTest</code>が
            ローカルで通ることを確認する（純粋なロジックを触った場合は<code>jacocoCoverageVerification</code>も）。
          </li>
          <li><code>main</code>に対してPRを開く — テンプレートが簡単なテストプランを求めます。</li>
          <li>CIが同じチェックを実行します。赤いチェックはマージ前に緑にする必要があります。</li>
        </ol>
      </section>

      <section>
        <h2>ライセンス</h2>
        <p>
          貢献することで、あなたの貢献がこのプロジェクトの{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            GPL-3.0 ライセンス
          </a>
          の下でライセンスされることに同意したものとみなされます。
        </p>
      </section>
    </LegalLayout>
  )
}
