import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="贡献指南"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="返回 OpenDisplay Android"
    >
      <p>
        感谢你考虑参与贡献。这是一个利用晚上和周末时间维护的个人项目 — 回复速度会有波动，但 bug
        报告和 pull request 真心欢迎。
      </p>

      <section>
        <h2>行为准则</h2>
        <p>
          本项目遵循<a href="code-of-conduct.html">Contributor Covenant</a>。参与时请遵守该准则。
        </p>
      </section>

      <section>
        <h2>报告问题 / 提议新功能</h2>
        <p>
          在 <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a> 上使用 bug
          报告或功能请求模板创建 issue。以下几点能大大加快处理速度：
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>证据。</strong> 能展示实际问题的截图、<code>adb logcat</code> 输出，或命令及其
            输出结果。粘贴前请遮盖任何 IP 地址或个人信息。
          </li>
          <li>
            如果是 bug，请附上<strong>设备型号和 Android 版本</strong>。
          </li>
          <li>先查一下已有的 issue —— 可能已经有人报告过了。</li>
        </ul>
      </section>

      <section>
        <h2>开发环境搭建</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # 构建
./gradlew installDebug           # 安装到已连接的设备/模拟器
./gradlew testDebugUnitTest      # 运行单元测试
adb logcat -s OpenDisplay:*      # 应用日志`}</code>
        </pre>
        <p>
          需要安装 Android SDK，并让 <code>ANDROID_HOME</code>/<code>local.properties</code> 指向它。
          完整的构建/使用文档见{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a>。
        </p>
      </section>

      <section>
        <h2>代码风格</h2>
        <ul>
          <li>
            <strong>始终选择能解决真实需求的最简单方案。</strong> 不做投机性的抽象、不做过早优化，
            不加"以后可能用得上"的未使用弹性。
          </li>
          <li>
            <strong>注释</strong>：代码中间不要散落 <code>{'//'}</code> 注释。类/函数的文档写在 KDoc 里；
            非显而易见的"为什么"写在 <code>RATIONALE.md</code>，而不是写成行内注释。
          </li>
          <li>
            使用 Kotlin + Jetpack Compose，遵循 <code>app/src/main/java/</code> 下已有的结构。
          </li>
        </ul>
      </section>

      <section>
        <h2>测试与覆盖率</h2>
        <p>
          纯逻辑代码（解析器、协议常量、不依赖 Android 框架的类）需要单元测试，将
          <strong>行覆盖率维持在 95% 以上</strong>（由 CI 中的 <code>jacocoCoverageVerification</code>{' '}
          校验）。UI、服务，以及依赖 <code>MediaCodec</code>/socket/<code>NsdManager</code> 的部分不计入
          该指标。
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>提交信息</h2>
        <p>
          写提交信息时，把它当成在向一个人解释这次改动，而不是生成更新日志 —— 说明<em>为什么</em>，
          不只是<em>做了什么</em>。避免使用像 <code>chore: ...</code> 这种没有实质内容的通用前缀。
        </p>
      </section>

      <section>
        <h2>Pull Request</h2>
        <ol>
          <li>
            从 <code>main</code> 拉出分支。
          </li>
          <li>保持改动聚焦 —— 一个 PR 只做一件事，更容易审查和合并。</li>
          <li>
            确认 <code>./gradlew assembleDebug</code> 和 <code>./gradlew testDebugUnitTest</code> 能在
            本地通过（如果改动了纯逻辑，<code>jacocoCoverageVerification</code> 也要通过）。
          </li>
          <li>
            针对 <code>main</code> 发起 PR —— 模板会要求填写简短的测试计划。
          </li>
          <li>CI 会运行相同的检查；合并前红色的检查项需要变绿。</li>
        </ol>
      </section>

      <section>
        <h2>许可证</h2>
        <p>
          参与贡献即表示你同意你的贡献将按本项目的{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            GPL-3.0 许可证
          </a>
          授权。
        </p>
      </section>
    </LegalLayout>
  )
}
