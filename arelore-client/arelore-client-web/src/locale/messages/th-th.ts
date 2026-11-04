import { SiteMessageTree } from './types';

const thTh: SiteMessageTree = {
  meta: {
    languageSwitcher: "ภาษาแม่"
  },
  common: {
    searchProducts: "ค้นหาผลิตภัณฑ์ เครื่องมือ หรือโซลูชัน…",
    browseAll: "ดูผลิตภัณฑ์ทั้งหมด",
    contactUs: "ติดต่อเรา",
    welcomeBack: "ยินดีต้อนรับกลับ, {name}!"
  },
  nav: {
    home: "หน้าแรก",
    products: "ผลิตภัณฑ์",
    foreignLanguage: "ภาษา",
    word: "Moyu Words",
    creator: "สร้างสรรค์",
    aiPrompt: "พรอมต์ AI",
    security: "ความปลอดภัย",
    databaseSecurity: "ความปลอดภัยฐานข้อมูล",
    databaseClassification: "การจัดประเภทข้อมูล",
    databaseAudit: "การตรวจสอบฐานข้อมูล",
    apiSecurity: "ความปลอดภัย API",
    applicationSecurity: "ความปลอดภัยแอป",
    codeSecurityScan: "สแกนความปลอดภัยโค้ด",
    mqSecurity: "ความปลอดภัย MQ",
    mqClassification: "การจัดประเภท MQ",
    tools: "เครื่องมือฟรี",
    textMasking: "การปกปิดข้อความ",
    fileDetection: "ตรวจจับเนื้อหาอ่อนไหว",
    documentProcessing: "เอกสาร",
    pdfToWord: "PDF เป็น Word",
    solutions: "โซลูชัน",
    scenarioSolutions: "โซลูชันตามสถานการณ์",
    databaseClassificationSolution: "การจัดประเภทและกำกับดูแลฐานข้อมูล",
    apiClassificationSolution: "การจัดประเภทและกำกับดูแล API",
    mqLogClassificationSolution: "การจัดประเภท MQ และล็อก",
    fileSensitiveSolution: "โซลูชันข้อมูลอ่อนไหวในไฟล์",
    contact: "ติดต่อ"
  },
  home: {
    heroAria: "หน้าแรก Arelore",
    heroTitle: "ส่งมอบความสามารถในรูปผลิตภัณฑ์",
    heroLead: "การเรียนภาษา การสร้างสรรค์ และความปลอดภัยข้อมูล — สแตกคลาวด์เดียวสำหรับเวิร์กโฟลว์จริง",
    sectionAria: "ผลิตภัณฑ์แนะนำ",
    sectionKicker: "Products",
    sectionTitle: "สายผลิตภัณฑ์",
    sectionLead: "เริ่มจากสถานการณ์หลัก หรือค้นหาในแคตตาล็อก"
  },
  products: {
    title: "ผลิตภัณฑ์",
    totalCount: "{count} ผลิตภัณฑ์ เครื่องมือ และโซลูชัน",
    resultCount: "ผลการค้นหา “{q}” ({count})",
    empty: "ไม่พบผลลัพธ์สำหรับ “{q}”"
  },
  kind: {
    product: "ผลิตภัณฑ์",
    tool: "เครื่องมือ",
    solution: "โซลูชัน"
  },
  catalog: {
    word: {
      title: "Moyu Words",
      category: "ภาษา",
      blurb: "เรียนคำศัพท์ในเซสชันสั้น: ดู ฟัง และเขียนตามคำบอก"
    },
    aiPrompt: {
      title: "พรอมต์ AI",
      category: "สร้างสรรค์",
      blurb: "จัดระเบียบพรอมต์ แท็ก และตัวช่วยสร้างสรรค์"
    },
    databaseClassification: {
      title: "การจัดประเภทข้อมูล",
      category: "ความปลอดภัยฐานข้อมูล",
      blurb: "สแกนตารางข้อมูลอ่อนไหวและสร้างโครงร่างการจัดประเภท"
    },
    databaseAudit: {
      title: "การตรวจสอบฐานข้อมูล",
      category: "ความปลอดภัยฐานข้อมูล",
      blurb: "ตรวจสอบการเข้าถึงและติดตามความเสี่ยง"
    },
    apiSecurity: {
      title: "ความปลอดภัย API",
      category: "ความปลอดภัย API",
      blurb: "ค้นหาและปกป้องสินทรัพย์ API"
    },
    codeSecurityScan: {
      title: "สแกนความปลอดภัยโค้ด",
      category: "ความปลอดภัยแอป",
      blurb: "สแกนความเสี่ยงของโค้ดและ dependency"
    },
    mqClassification: {
      title: "การจัดประเภท MQ",
      category: "ความปลอดภัย MQ",
      blurb: "จัดประเภทเนื้อหาอ่อนไหวในคิวข้อความ"
    },
    textMasking: {
      title: "การปกปิดข้อความ",
      category: "เครื่องมือฟรี",
      blurb: "ปกปิดข้อความอ่อนไหวออนไลน์"
    },
    fileDetection: {
      title: "ตรวจจับเนื้อหาอ่อนไหว",
      category: "เครื่องมือฟรี",
      blurb: "อัปโหลดไฟล์เพื่อตรวจจับเนื้อหาอ่อนไหว"
    },
    pdfToWord: {
      title: "PDF เป็น Word",
      category: "เอกสาร",
      blurb: "แปลง PDF เป็น Word"
    },
    databaseClassificationSolution: {
      title: "การจัดประเภทและกำกับดูแลฐานข้อมูล",
      category: "โซลูชันตามสถานการณ์",
      blurb: "นำการจัดประเภทฐานข้อมูลสู่การกำกับดูแลจริง"
    },
    apiClassificationSolution: {
      title: "การจัดประเภทและกำกับดูแล API",
      category: "โซลูชันตามสถานการณ์",
      blurb: "การจัดประเภท API พร้อมการควบคุมการปฏิบัติตาม"
    },
    mqLogClassificationSolution: {
      title: "การจัดประเภท MQ และล็อก",
      category: "โซลูชันตามสถานการณ์",
      blurb: "จัดประเภทข้อมูลอ่อนไหวใน MQ และล็อก"
    },
    fileSensitiveSolution: {
      title: "โซลูชันข้อมูลอ่อนไหวในไฟล์",
      category: "โซลูชันตามสถานการณ์",
      blurb: "ค้นหาและปกป้องไฟล์อ่อนไหว"
    }
  },
  footer: {
    createdBy: "Arelore ©{year} Created by Arelore Team",
    about: "เกี่ยวกับเรา",
    privacy: "นโยบายความเป็นส่วนตัว",
    terms: "ข้อกำหนดการใช้บริการ",
    help: "ศูนย์ช่วยเหลือ",
    icp: "沪ICP备2026055763号"
  }
};

export default thTh;
