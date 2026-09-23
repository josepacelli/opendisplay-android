import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="개인정보처리방침"
      subtitle="OpenDisplay Android · 최종 업데이트 2026-08-15"
      page="privacy"
      backLabel="OpenDisplay Android로 돌아가기"
    >
      <p>
        OpenDisplay Android는 오리지널{' '}
        <a href="https://opendisplay.app/">OpenDisplay</a> 앱을 실행하는 Mac의 세컨드 모니터로
        Android 기기를 바꿔주는 오픈소스 클라이언트입니다. 이 방침은 앱이 여러분의 데이터로 무엇을
        하고, 무엇을 하지 않는지 설명합니다.
      </p>

      <section>
        <h2>요약</h2>
        <ul>
          <li>계정, 가입, 로그인 없음.</li>
          <li>분석, 크래시 리포트, 광고 SDK 없음.</li>
          <li>개발자가 운영하는 서버로 전송되는 데이터 없음 — 애초에 그런 서버가 존재하지 않습니다.</li>
          <li>
            모든 네트워크 트래픽은 여러분의 Android 기기와 여러분 자신의 Mac 사이의 직접 연결이며,
            로컬 네트워크(또는 <code>adb forward</code>를 사용한 USB 케이블) 경유입니다.
          </li>
        </ul>
      </section>

      <section>
        <h2>앱이 네트워크에서 하는 일</h2>
        <p>
          앱은 로컬 TCP 포트에서 대기하며 mDNS(<code>_opensidecar._tcp</code>)로 자신을 알려서
          OpenDisplay Mac 앱이 같은 네트워크에서 찾아 연결할 수 있게 합니다. 연결되면 해당 Mac과
          직접 비디오 프레임(Mac의 화면 내용)과 입력 이벤트(여러분이 다시 보내는 터치/스크롤)를
          주고받습니다 — 제3자나 개발자 소유의 어떤 서버도 거치거나 저장하지 않습니다.
        </p>
      </section>

      <section>
        <h2>사용하는 권한</h2>
        <table>
          <tbody>
            <tr>
              <th>권한</th>
              <th>이유</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Mac이 연결할 로컬 TCP 소켓을 엽니다. 위에서 설명한 Mac과의 직접 연결에만 사용되며,
                인터넷상의 어떤 서비스에도 사용되지 않습니다.
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>로컬 WiFi 네트워크에서의 mDNS 검색. Mac이 기기를 자동으로 찾을 수 있게 합니다.</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                앱이 외부 디스플레이 역할을 하는 동안, 화면에 표시된 앱이 아니더라도 Mac과의 연결을
                유지합니다.
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>위 포그라운드 서비스에 필요한 상태 알림을 표시합니다.</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>데이터 저장</h2>
        <p>
          앱이 기기에 로컬로 저장하는 유일한 것은 자체 설정(mDNS 기기 이름)입니다 — 앱의 로컬
          설정에 저장되며, 어디로도 전송되지 않고, 앱을 삭제하면 함께 제거됩니다.
        </p>
      </section>

      <section>
        <h2>제3자</h2>
        <p>
          없습니다. 이 앱에는 서드파티 SDK, 광고 네트워크, 분석 제공업체, 클라우드 백엔드가 전혀
          없습니다.
        </p>
      </section>

      <section>
        <h2>이 방침의 변경</h2>
        <p>
          앱이 데이터를 다루는 방식에 변경이 있으면 이 페이지에 반영되며, 위의 "최종 업데이트" 날짜도
          함께 갱신됩니다.
        </p>
      </section>

      <section>
        <h2>문의</h2>
        <p>
          질문이나 문의사항은{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub 저장소</a>에
          이슈를 등록해 주세요.
        </p>
      </section>
    </LegalLayout>
  )
}
