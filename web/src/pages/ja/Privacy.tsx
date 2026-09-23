import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="プライバシーポリシー"
      subtitle="OpenDisplay Android · 最終更新日 2026-08-15"
      page="privacy"
      backLabel="OpenDisplay Androidに戻る"
    >
      <p>
        OpenDisplay Androidは、オリジナルの<a href="https://opendisplay.app/">OpenDisplay</a>
        アプリを実行しているMacのセカンドモニターにAndroid端末を変えるオープンソースクライアントです。
        このポリシーは、本アプリがあなたのデータに対して何を行い、何を行わないかを説明します。
      </p>

      <section>
        <h2>要約</h2>
        <ul>
          <li>アカウント登録もサインアップもログインも不要。</li>
          <li>アナリティクスなし、クラッシュレポートなし、広告SDKなし。</li>
          <li>開発者が運営するサーバーへのデータ送信は一切なし — そもそもそのようなサーバーは存在しません。</li>
          <li>
            すべてのネットワーク通信は、あなたのAndroid端末とあなた自身のMacとの間の直接接続で、
            ローカルネットワーク経由（または<code>adb forward</code>によるUSBケーブル経由）です。
          </li>
        </ul>
      </section>

      <section>
        <h2>アプリがネットワーク上で行うこと</h2>
        <p>
          アプリはローカルのTCPポートで待ち受け、mDNS（<code>_opensidecar._tcp</code>）で自身を通知することで、
          OpenDisplay Macアプリが同じネットワーク上でこれを見つけて接続できるようにします。接続後は、そのMacと
          直接、ビデオフレーム（Macの画面内容）と入力イベント（あなたが送り返すタッチ/スクロール）をやり取りします
          — 第三者や開発者が所有するサーバーを経由したり、そこに保存されたりすることは一切ありません。
        </p>
      </section>

      <section>
        <h2>使用する権限</h2>
        <table>
          <tbody>
            <tr>
              <th>権限</th>
              <th>理由</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Macが接続するローカルTCPソケットを開きます。上記のMacとの直接接続のためだけに使用され、
                インターネット上のいかなるサービスにも使われません。
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>ローカルWiFiネットワーク上でのmDNS検出。Macが端末を自動的に見つけられるようにします。</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                アプリが外部ディスプレイとして機能している間、たとえ画面に表示されているアプリでなくても、
                Macとの接続を維持します。
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>上記のフォアグラウンドサービスに必要なステータス通知を表示します。</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>データの保存</h2>
        <p>
          アプリが端末にローカルで保持する唯一のものは、自身の設定（mDNSデバイス名）です — アプリのローカル設定に
          保存され、どこにも送信されず、アプリをアンインストールすれば削除されます。
        </p>
      </section>

      <section>
        <h2>第三者</h2>
        <p>
          ありません。本アプリにはサードパーティSDK、広告ネットワーク、アナリティクスプロバイダー、
          クラウドバックエンドのいずれもありません。
        </p>
      </section>

      <section>
        <h2>このポリシーの変更</h2>
        <p>
          アプリがデータをどう扱うかに変更があれば、このページに反映され、上記の「最終更新日」も更新されます。
        </p>
      </section>

      <section>
        <h2>お問い合わせ</h2>
        <p>
          ご質問やご懸念は、{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">
            GitHubリポジトリ
          </a>
          でissueを立ててください。
        </p>
      </section>
    </LegalLayout>
  )
}
