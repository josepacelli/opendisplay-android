import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="기여 방법"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="OpenDisplay Android로 돌아가기"
    >
      <p>
        기여를 고려해 주셔서 감사합니다. 이 프로젝트는 평일 저녁과 주말에 혼자 관리하는 개인 프로젝트라
        응답 속도는 그때그때 다르지만, 버그 신고와 풀 리퀘스트는 정말 환영합니다.
      </p>

      <section>
        <h2>행동 강령</h2>
        <p>
          이 프로젝트는 <a href="code-of-conduct.html">Contributor Covenant</a>를 따릅니다. 참여할
          때는 이를 지켜주시기 바랍니다.
        </p>
      </section>

      <section>
        <h2>버그 신고 / 기능 제안</h2>
        <p>
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a>에서 버그
          신고 또는 기능 요청 템플릿을 사용해 issue를 열어주세요. 대응 속도를 확실히 높여주는 몇 가지:
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>증거.</strong> 실제 동작을 보여주는 스크린샷, <code>adb logcat</code> 출력, 또는
            명령어와 그 결과. 붙여넣기 전에 IP 주소나 개인정보는 가려주세요.
          </li>
          <li>버그라면 <strong>기기와 Android 버전</strong>.</li>
          <li>먼저 기존 issue를 확인해 주세요 — 이미 등록되어 있을 수 있습니다.</li>
        </ul>
      </section>

      <section>
        <h2>개발 환경 설정</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # 빌드
./gradlew installDebug           # 연결된 기기/에뮬레이터에 설치
./gradlew testDebugUnitTest      # 단위 테스트 실행
adb logcat -s OpenDisplay:*      # 앱 로그`}</code>
        </pre>
        <p>
          Android SDK가 설치되어 있고 <code>ANDROID_HOME</code>/<code>local.properties</code>가
          이를 가리키고 있어야 합니다. 전체 빌드/사용 문서는{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a>를
          참고하세요.
        </p>
      </section>

      <section>
        <h2>코드 스타일</h2>
        <ul>
          <li>
            <strong>실제 필요를 해결하는 가장 단순한 방법을 항상 우선.</strong> 투기적인 추상화,
            이른 최적화, "나중을 위한" 미사용 유연성은 지양합니다.
          </li>
          <li>
            <strong>주석</strong>: 코드 중간에 흩어진 <code>{'//'}</code> 주석은 쓰지 않습니다.
            클래스/함수 문서는 KDoc에 작성합니다. 자명하지 않은 이유는 인라인이 아니라{' '}
            <code>RATIONALE.md</code>에 적습니다.
          </li>
          <li>
            Kotlin + Jetpack Compose로, <code>app/src/main/java/</code>에 이미 있는 구조를 따릅니다.
          </li>
        </ul>
      </section>

      <section>
        <h2>테스트와 커버리지</h2>
        <p>
          순수 로직(파서, 프로토콜 상수, Android 프레임워크에 의존하지 않는 클래스)은{' '}
          <strong>라인 커버리지 95% 이상</strong>을 유지하는 단위 테스트가 필요합니다(CI의{' '}
          <code>jacocoCoverageVerification</code>로 검증). UI, 서비스, <code>MediaCodec</code>/소켓/
          <code>NsdManager</code>에 의존하는 것들은 이 계산에서 제외됩니다.
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>커밋 메시지</h2>
        <p>
          체인지로그를 생성하듯이 쓰지 말고, 사람에게 변경 사항을 설명하듯 써주세요 — <em>무엇을</em>{' '}
          했는지뿐 아니라 <em>왜</em>인지를 설명합니다. 내용 없는 <code>chore: ...</code> 같은 일반적인
          접두사는 피해주세요.
        </p>
      </section>

      <section>
        <h2>풀 리퀘스트</h2>
        <ol>
          <li><code>main</code>에서 브랜치를 만듭니다.</li>
          <li>변경 사항을 좁게 유지하세요 — PR 하나에 하나의 주제가 리뷰와 머지를 더 쉽게 만듭니다.</li>
          <li>
            <code>./gradlew assembleDebug</code>와 <code>./gradlew testDebugUnitTest</code>가
            로컬에서 통과하는지 확인하세요(순수 로직을 건드렸다면{' '}
            <code>jacocoCoverageVerification</code>도).
          </li>
          <li><code>main</code>을 대상으로 PR을 여세요 — 템플릿에서 간단한 테스트 계획을 물어봅니다.</li>
          <li>CI가 같은 검사를 실행합니다. 빨간 체크는 머지 전에 초록으로 바뀌어야 합니다.</li>
        </ol>
      </section>

      <section>
        <h2>라이선스</h2>
        <p>
          기여함으로써, 당신의 기여가 이 프로젝트의{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            GPL-3.0 라이선스
          </a>{' '}
          하에 라이선스됨에 동의하는 것으로 간주됩니다.
        </p>
      </section>
    </LegalLayout>
  )
}
