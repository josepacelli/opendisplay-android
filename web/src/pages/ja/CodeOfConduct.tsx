import { LegalLayout } from '@/components/layout/LegalLayout'

export function CodeOfConduct() {
  return (
    <LegalLayout
      title="行動規範"
      subtitle="OpenDisplay Android · Contributor Covenant v2.1"
      page="code-of-conduct"
      backLabel="OpenDisplay Androidに戻る"
    >
      <section>
        <h2>私たちの誓い</h2>
        <p>
          このプロジェクトのメンバー、貢献者、リーダーとして、私たちは年齢、体格、目に見える障害や見えない障害、
          民族性、性的特徴、性自認と性表現、経験レベル、学歴、社会経済的地位、国籍、外見、人種、宗教、性的指向に
          かかわらず、すべての人にとってハラスメントのないコミュニティ参加体験を実現することを誓います。
        </p>
        <p className="mt-2.5">
          私たちは、オープンで、歓迎的で、多様で、包摂的で、健全なコミュニティに貢献する形で行動し、交流することを誓います。
        </p>
      </section>

      <section>
        <h2>私たちの基準</h2>
        <p>ポジティブな環境に貢献する行動の例:</p>
        <ul className="mt-2.5">
          <li>他者への共感と親切さを示すこと</li>
          <li>異なる意見、視点、経験を尊重すること</li>
          <li>建設的なフィードバックを与え、また快く受け入れること</li>
          <li>自分の過ちに影響を受けた人々に責任を持ち、謝罪し、その経験から学ぶこと</li>
          <li>個人としてだけでなく、コミュニティ全体にとって最善のことに焦点を当てること</li>
        </ul>
        <p className="mt-4">許容されない行動の例:</p>
        <ul className="mt-2.5">
          <li>性的な言葉や画像の使用、あらゆる種類の性的な注目や誘い</li>
          <li>荒らし行為、侮辱的・中傷的なコメント、個人攻撃や政治的攻撃</li>
          <li>公的・私的を問わない嫌がらせ</li>
          <li>本人の明確な許可なく、住所やメールアドレスなど他者の個人情報を公開すること</li>
          <li>その他、専門的な場において不適切と合理的に判断される行動</li>
        </ul>
      </section>

      <section>
        <h2>運用の責任</h2>
        <p>
          プロジェクトの管理者は、許容される行動の基準を明確化し運用する責任を負い、不適切、脅迫的、攻撃的、
          または有害と判断される行動に対して、適切かつ公正な是正措置を取ります。
        </p>
      </section>

      <section>
        <h2>適用範囲</h2>
        <p>
          この行動規範は、このリポジトリのissue、pull request、ディスカッションなど、すべてのコミュニティ空間に
          適用され、また個人が公的な場でコミュニティを公式に代表している場合にも適用されます。
        </p>
      </section>

      <section>
        <h2>報告方法</h2>
        <p>
          虐待的、嫌がらせ、またはその他の許容できない行動の事例は、管理者{' '}
          <a href="mailto:josepacelli@gmail.com">josepacelli@gmail.com</a> まで報告できます。すべての申し立ては
          迅速かつ公正にレビュー・調査されます。管理者は報告者のプライバシーと安全を尊重する義務を負います。
        </p>
      </section>

      <section>
        <h2>運用ガイドライン</h2>
        <p>
          <strong>1. 是正</strong> — 不適切な言葉遣いやその他の非専門的な行動。結果: 違反内容を説明する非公開の
          書面による警告。
        </p>
        <p className="mt-2.5">
          <strong>2. 警告</strong> — 単発の事案、または一連の行動による違反。結果: 一定期間、関係者との交流を
          行わないことを含む、継続的な行動への結果を伴う警告。
        </p>
        <p className="mt-2.5">
          <strong>3. 一時的な禁止</strong> — コミュニティ基準への重大な違反。結果: コミュニティとのあらゆる交流・
          公的コミュニケーションの一時的な禁止。
        </p>
        <p className="mt-2.5">
          <strong>4. 永久的な禁止</strong> — 個人への嫌がらせや特定の人々への攻撃性を含む、違反パターンの継続。
          結果: コミュニティ内でのあらゆる公的な交流の永久的な禁止。
        </p>
      </section>

      <section>
        <h2>クレジット</h2>
        <p>
          <a href="https://www.contributor-covenant.org/version/2/1/code_of_conduct.html">
            Contributor Covenant
          </a>{' '}
          バージョン2.1を基に作成。全文と各言語訳は{' '}
          <a href="https://www.contributor-covenant.org">contributor-covenant.org</a> を参照してください。
        </p>
      </section>
    </LegalLayout>
  )
}
