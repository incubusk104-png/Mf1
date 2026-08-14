import { PolicyLayout, type PolicySection } from "@/components/PolicyLayout";
import { site } from "@/lib/site";

const sections: PolicySection[] = [
  {
    heading: "一、我们是谁",
    paragraphs: [
      `"Mindset Frames"（应用包名：${site.packageName}）是一款以心情为先的习惯打卡安卓应用，由 ${site.developerName}（下称"我们"）通过华为应用市场（AppGallery）发布。本隐私政策说明 Mindset Frames 应用及本网站处理哪些数据、处理目的，以及你享有的权利与选择。`,
      `如有任何疑问，欢迎随时通过 ${site.supportEmail} 与我们联系。`,
    ],
  },
  {
    heading: "二、简要说明",
    paragraphs: [],
    bullets: [
      "Mindset Frames 采用本地优先设计：习惯、签到、心情与反思默认仅存储在你的设备上。",
      "云端备份为可选功能，仅在你主动注册并登录后才会启用。",
      "应用不展示任何广告，不内置广告或分析 SDK，绝不出售或共享你的数据用于营销。",
      "你可以随时在应用内永久删除帐号及全部云端数据。",
      "应用服务器位于中国大陆境外。",
    ],
  },
  {
    heading: "三、存储在你设备上的数据",
    paragraphs: [
      "应用将你的习惯、每日签到、心情历史、单行反思、已获徽章、伙伴形象及设置保存在设备的私有应用存储中。除非你开启云端备份，这些数据不会离开你的设备。卸载应用即会删除这些数据。",
    ],
  },
  {
    heading: "四、开启云端备份后处理的数据",
    paragraphs: ["若你选择创建帐号，我们仅处理运行备份与恢复所需的最少数据："],
    bullets: [
      "邮箱登录：你的邮箱地址与密码。密码仅以加盐哈希形式由我们的认证服务存储 — 我们无法看到或以明文保存密码。",
      "华为帐号登录：在你同意后，仅请求最小权限 — OpenID 标识符与邮箱地址。登录凭证会经由华为帐号服务在服务器端验证。我们不会访问你的通讯录、手机号或其他华为帐号资料。",
      "备份内容：你的习惯、签到日期、心情历史与应用设置，关联至一个随机的帐号标识符。",
      "每次安装生成的随机设备标识符（UUID），仅用于同步记录。它不是广告标识符，也不来源于任何硬件标识。",
    ],
  },
  {
    heading: "五、处理目的与法律依据",
    paragraphs: [
      "我们处理帐号数据的目的仅为提供备份、恢复与多设备同步（履行合同）、保障帐号安全（合法利益），并且仅在你主动登录后进行（同意 — 你可通过退出登录或删除帐号随时撤回）。我们不会对你进行画像，也不会做出任何自动化决策。",
    ],
  },
  {
    heading: "六、数据存储位置",
    paragraphs: [
      "云端数据存储于 Supabase 运营的托管 PostgreSQL 数据库中，服务器位于中国大陆境外。所有传输中的数据均采用 TLS 1.2 及以上加密。在你的设备上，会话令牌由 Android Keystore 中的硬件级密钥加密保存。",
    ],
  },
  {
    heading: "七、第三方服务",
    paragraphs: ["我们依赖以下两家服务提供商，且各自仅接收必要信息："],
    bullets: [
      "Supabase（认证与数据库托管）— 接收你的帐号邮箱或由华为帐号派生的帐号身份，以及传输加密的备份内容。",
      "华为帐号服务 Account Kit（可选的华为帐号登录）— 依据华为自身的隐私声明处理你的华为帐号登录；我们仅接收经验证的 OpenID 与邮箱。",
    ],
  },
  {
    heading: "八、数据保留与删除",
    paragraphs: [
      "云端数据在帐号存续期间保留。在设置中选择\u201c删除帐号\u201d，将在一次服务器端事务中永久清除你名下的全部云端记录 — 习惯、签到、心情、设置及帐号本身。本地数据保留在你的设备上，直至你清除应用存储或卸载应用。",
    ],
  },
  {
    heading: "九、你的权利",
    paragraphs: [
      "依据你所在地区的法律（包括欧盟/英国 GDPR 以及《个人信息保护法》等），你有权访问、更正、导出、限制或删除个人数据，并可随时撤回同意。大多数操作可在应用内自助完成；其他请求请发送邮件，我们将在 30 天内回复。你也有权向当地监管机构投诉。",
    ],
  },
  {
    heading: "十、未成年人保护",
    paragraphs: [
      "Mindset Frames 不面向 16 周岁以下儿童，我们不会有意收集儿童个人数据。若你发现儿童创建了帐号，请联系我们删除。未成年人应在监护人同意后再使用应用的可选帐号功能。",
    ],
  },
  {
    heading: "十一、应用权限",
    paragraphs: ["应用仅申请功能所需的权限："],
    bullets: [
      "通知 — 用于你配置的每日提醒、连续记录提醒与每周回顾。全部在设备本地生成，无推送服务器。",
      "网络及网络状态 — 仅用于可选的云端备份。",
      "开机自启动广播 — 用于设备重启后重新安排你的提醒。",
      "不申请相机、麦克风、位置、通讯录权限；除你主动导出的分享图片外，不访问存储。",
    ],
  },
  {
    heading: "十二、安全措施",
    paragraphs: [
      "每条云端数据均按帐号以严格的行级安全策略隔离；登录令牌在设备上加密存储；华为帐号登录在签发任何帐号前都会经服务器端向华为帐号服务验证；所有连接均使用 HTTPS。虽然任何存储方式都无法保证绝对安全，但我们的设计确保即使是服务器代码也只能访问其必需的内容。",
    ],
  },
  {
    heading: "十三、政策更新",
    paragraphs: [
      "若本政策发生变更，新版本将发布在本页面并更新生效日期。重大变更会在生效前于应用内另行提示。",
    ],
  },
  {
    heading: "十四、联系方式",
    paragraphs: [
      `开发者：${site.developerName}`,
      `应用：Mindset Frames（${site.packageName}）`,
      `邮箱：${site.supportEmail}`,
      `网站：${site.domain}`,
    ],
  },
];

export default function PrivacyPolicyZh() {
  return (
    <PolicyLayout
      title="隐私政策"
      updatedLabel={`生效日期：${site.privacyEffectiveDate} · Mindset Frames`}
      backLabel="返回 mindsetframes.online"
      altVersionLabel="English version"
      altVersionTo="/privacy"
      sections={sections}
    />
  );
}
