import { LegalLayout } from '@/components/layout/LegalLayout'

export function CodeOfConduct() {
  return (
    <LegalLayout
      title="行为准则"
      subtitle="OpenDisplay Android · Contributor Covenant v2.1"
      page="code-of-conduct"
      backLabel="返回 OpenDisplay Android"
    >
      <section>
        <h2>我们的承诺</h2>
        <p>
          作为本项目的成员、贡献者和负责人，我们承诺让所有人参与我们社区的体验都不受骚扰困扰，无论年龄、体型、
          外显或非外显的残障、民族、性征、性别认同与表达、经验水平、教育程度、社会经济地位、国籍、外貌、
          种族、宗教信仰，或性取向如何。
        </p>
        <p className="mt-2.5">我们承诺以有助于建设一个开放、友善、多元、包容、健康社区的方式行事和互动。</p>
      </section>

      <section>
        <h2>我们的准则</h2>
        <p>有助于营造积极环境的行为示例：</p>
        <ul className="mt-2.5">
          <li>对他人表现出同理心和善意</li>
          <li>尊重不同的意见、观点和经历</li>
          <li>给予并优雅地接受建设性反馈</li>
          <li>为自己的错误承担责任，向受影响的人道歉，并从中吸取经验</li>
          <li>不仅关注个人利益，更关注整个社区的最佳利益</li>
        </ul>
        <p className="mt-4">不可接受行为的示例：</p>
        <ul className="mt-2.5">
          <li>使用与性有关的语言或图像，以及任何形式的性关注或性暗示</li>
          <li>挑衅、侮辱性或贬低性言论，以及人身攻击或政治攻击</li>
          <li>公开或私下的骚扰行为</li>
          <li>未经明确许可发布他人的私人信息，例如实际地址或电子邮件地址</li>
          <li>在专业场合中可能被合理认为不适当的其他行为</li>
        </ul>
      </section>

      <section>
        <h2>执行责任</h2>
        <p>
          项目维护者负责澄清并执行我们可接受行为的准则，并会针对任何被认为不适当、具有威胁性、冒犯性或
          有害的行为采取适当且公正的纠正措施。
        </p>
      </section>

      <section>
        <h2>适用范围</h2>
        <p>
          本行为准则适用于本仓库的所有社区空间（issue、pull request、讨论），也适用于个人在公共场合正式
          代表社区的情形。
        </p>
      </section>

      <section>
        <h2>如何举报</h2>
        <p>
          辱骂、骚扰或其他不可接受行为的实例可以举报给维护者，邮箱：{' '}
          <a href="mailto:josepacelli@gmail.com">josepacelli@gmail.com</a>。所有投诉都会得到及时、公正的审查
          和调查。维护者有义务尊重举报人的隐私和安全。
        </p>
      </section>

      <section>
        <h2>执行准则</h2>
        <p>
          <strong>1. 纠正</strong> — 使用不当语言或其他不专业行为。后果：私下发出书面警告，说明违规内容。
        </p>
        <p className="mt-2.5">
          <strong>2. 警告</strong> — 单次事件或一系列行为造成的违规。后果：警告并附带对持续行为的后果，
          包括在规定期限内不得与相关人员互动。
        </p>
        <p className="mt-2.5">
          <strong>3. 临时封禁</strong> — 严重违反社区准则。后果：在一定期限内禁止与社区的任何互动或公开
          交流。
        </p>
        <p className="mt-2.5">
          <strong>4. 永久封禁</strong> — 表现出持续违规模式，包括骚扰个人或针对特定群体的攻击性行为。
          后果：永久禁止在社区内进行任何公开互动。
        </p>
      </section>

      <section>
        <h2>致谢</h2>
        <p>
          本准则改编自{' '}
          <a href="https://www.contributor-covenant.org/version/2/1/code_of_conduct.html">
            Contributor Covenant
          </a>{' '}
          2.1 版。完整文本及各语言译本请见{' '}
          <a href="https://www.contributor-covenant.org">contributor-covenant.org</a>。
        </p>
      </section>
    </LegalLayout>
  )
}
