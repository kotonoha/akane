package ws.kotonoha.akane.dic.jmdict

case class JmdictTagInfo(tag: JmdictTag, repr: String, explanation: String) {
  def number: Int = tag.value
}

object JmdictTagMap {
  val tagMap = Map[String, JmdictTag](
    //martial arts term
    "MA" -> JmdictTag.MA,
    //rude or X-rated term (not displayed in educational software)
    "X" -> JmdictTag.X,
    //abbreviation
    "abbr" -> JmdictTag.abbr,
    //adjective (keiyoushi)
    "adj-i" -> JmdictTag.adjI,
    //adjectival nouns or quasi-adjectives (keiyodoshi)
    "adj-na" -> JmdictTag.adjNa,
    //nouns which may take the genitive case particle `no'
    "adj-no" -> JmdictTag.adjNo,
    //pre-noun adjectival (rentaishi)
    "adj-pn" -> JmdictTag.adjPn,
    //`taru' adjective
    "adj-t" -> JmdictTag.adjT,
    //noun or verb acting prenominally
    "adj-f" -> JmdictTag.adjF,
    //former adjective classification (being removed)
    "adj" -> JmdictTag.adj,
    //adverb (fukushi)
    "adv" -> JmdictTag.adv,
    //adverb taking the `to' particle
    "adv-to" -> JmdictTag.advTo,
    //archaism
    "arch" -> JmdictTag.arch,
    //ateji (phonetic) reading
    "ateji" -> JmdictTag.ateji,
    //auxiliary
    "aux" -> JmdictTag.aux,
    //auxiliary verb
    "aux-v" -> JmdictTag.auxV,
    //auxiliary adjective
    "aux-adj" -> JmdictTag.auxAdj,
    //Buddhist term
    "Buddh" -> JmdictTag.Buddh,
    //chemistry term
    "chem" -> JmdictTag.chem,
    //children's language
    "chn" -> JmdictTag.chn,
    //colloquialism
    "col" -> JmdictTag.col,
    //computer terminology
    "comp" -> JmdictTag.comp,
    //conjunction
    "conj" -> JmdictTag.conj,
    //counter
    "ctr" -> JmdictTag.ctr,
    //derogatory
    "derog" -> JmdictTag.derog,
    //exclusively kanji
    "eK" -> JmdictTag.exclKanj,
    //exclusively kana
    "ek" -> JmdictTag.exclKana,
    //Expressions (phrases, clauses, etc.)
    "exp" -> JmdictTag.exp,
    //familiar language
    "fam" -> JmdictTag.fam,
    //female term or language
    "fem" -> JmdictTag.fem,
    //food term
    "food" -> JmdictTag.food,
    //geometry term
    "geom" -> JmdictTag.geom,
    //gikun (meaning as reading)  or jukujikun (special kanji reading)
    "gikun" -> JmdictTag.gikun,
    //honorific or respectful (sonkeigo) language
    "hon" -> JmdictTag.hon,
    //humble (kenjougo) language
    "hum" -> JmdictTag.hum,
    //word containing irregular kanji usage
    "iK" -> JmdictTag.irrKanji,
    //idiomatic expression
    "id" -> JmdictTag.id,
    //word containing irregular kana usage
    "ik" -> JmdictTag.irrKana,
    //interjection (kandoushi)
    "int" -> JmdictTag.interj,
    //irregular okurigana usage
    "io" -> JmdictTag.io,
    //irregular verb
    "iv" -> JmdictTag.iv,
    //linguistics terminology
    "ling" -> JmdictTag.ling,
    //manga slang
    "m-sl" -> JmdictTag.mSl,
    //male term or language
    "male" -> JmdictTag.male,
    //male slang
    "male-sl" -> JmdictTag.maleSl,
    //mathematics
    "math" -> JmdictTag.math,
    //military
    "mil" -> JmdictTag.mil,
    //noun (common) (futsuumeishi)
    "n" -> JmdictTag.n,
    //adverbial noun (fukushitekimeishi)
    "n-adv" -> JmdictTag.nAdv,
    //noun, used as a suffix
    "n-suf" -> JmdictTag.nSuf,
    //noun, used as a prefix
    "n-pref" -> JmdictTag.nPref,
    //noun (temporal) (jisoumeishi)
    "n-t" -> JmdictTag.nT,
    //numeric
    "num" -> JmdictTag.num,
    //word containing out-dated kanji
    "oK" -> JmdictTag.outKanji,
    //obsolete term
    "obs" -> JmdictTag.obs,
    //obscure term
    "obsc" -> JmdictTag.obsc,
    //out-dated or obsolete kana usage
    "ok" -> JmdictTag.outKana,
    //onomatopoeic or mimetic word
    "on-mim" -> JmdictTag.onMim,
    //pronoun
    "pn" -> JmdictTag.pn,
    //poetical term
    "poet" -> JmdictTag.poet,
    //polite (teineigo) language
    "pol" -> JmdictTag.pol,
    //prefix
    "pref" -> JmdictTag.pref,
    //proverb
    "proverb" -> JmdictTag.proverb,
    //particle
    "prt" -> JmdictTag.prt,
    //physics terminology
    "physics" -> JmdictTag.physics,
    //rare
    "rare" -> JmdictTag.rare,
    //sensitive
    "sens" -> JmdictTag.sens,
    //slang
    "sl" -> JmdictTag.sl,
    //suffix
    "suf" -> JmdictTag.suf,
    //word usually written using kanji alone
    "uK" -> JmdictTag.useKanji,
    //word usually written using kana alone
    "uk" -> JmdictTag.useKana,
    //Ichidan verb
    "v1" -> JmdictTag.v1,
    //Nidan verb with 'u' ending (archaic)
    "v2a-s" -> JmdictTag.v2aS,
    //Yondan verb with `hu/fu' ending (archaic)
    "v4h" -> JmdictTag.v4h,
    //Yondan verb with `ru' ending (archaic)
    "v4r" -> JmdictTag.v4r,
    //Godan verb (not completely classified)
    "v5" -> JmdictTag.v5,
    //Godan verb - -aru special class
    "v5aru" -> JmdictTag.v5aru,
    //Godan verb with `bu' ending
    "v5b" -> JmdictTag.v5b,
    //Godan verb with `gu' ending
    "v5g" -> JmdictTag.v5g,
    //Godan verb with `ku' ending
    "v5k" -> JmdictTag.v5k,
    //Godan verb - Iku/Yuku special class
    "v5k-s" -> JmdictTag.v5kS,
    //Godan verb with `mu' ending
    "v5m" -> JmdictTag.v5m,
    //Godan verb with `nu' ending
    "v5n" -> JmdictTag.v5n,
    //Godan verb with `ru' ending
    "v5r" -> JmdictTag.v5r,
    //Godan verb with `ru' ending (irregular verb)
    "v5r-i" -> JmdictTag.v5rI,
    //Godan verb with `su' ending
    "v5s" -> JmdictTag.v5s,
    //Godan verb with `tsu' ending
    "v5t" -> JmdictTag.v5t,
    //Godan verb with `u' ending
    "v5u" -> JmdictTag.v5u,
    //Godan verb with `u' ending (special class)
    "v5u-s" -> JmdictTag.v5uS,
    //Godan verb - Uru old class verb (old form of Eru)
    "v5uru" -> JmdictTag.v5uru,
    //Godan verb with `zu' ending
    "v5z" -> JmdictTag.v5z,
    //Ichidan verb - zuru verb (alternative form of -jiru verbs)
    "vz" -> JmdictTag.vz,
    //intransitive verb
    "vi" -> JmdictTag.vi,
    //Kuru verb - special class
    "vk" -> JmdictTag.vk,
    //irregular nu verb
    "vn" -> JmdictTag.vn,
    //irregular ru verb, plain form ends with -ri
    "vr" -> JmdictTag.vr,
    //noun or participle which takes the aux. verb suru
    "vs" -> JmdictTag.vs,
    //su verb - precursor to the modern suru
    "vs-c" -> JmdictTag.vsC,
    //suru verb - special class
    "vs-s" -> JmdictTag.vsS,
    //suru verb - irregular
    "vs-i" -> JmdictTag.vsI,
    //Kyoto-ben
    "kyb" -> JmdictTag.kyb,
    //Osaka-ben
    "osb" -> JmdictTag.osb,
    //Kansai-ben
    "ksb" -> JmdictTag.ksb,
    //Kantou-ben
    "ktb" -> JmdictTag.ktb,
    //Tosa-ben
    "tsb" -> JmdictTag.tsb,
    //Touhoku-ben
    "thb" -> JmdictTag.thb,
    //Tsugaru-ben
    "tsug" -> JmdictTag.tsug,
    //Kyuushuu-ben
    "kyu" -> JmdictTag.kyu,
    //Ryuukyuu-ben
    "rkb" -> JmdictTag.rkb,
    //Nagano-ben
    "nab" -> JmdictTag.nab,
    //transitive verb
    "vt" -> JmdictTag.vt,
    //vulgar expression or word
    "vulg" -> JmdictTag.vulg
  )
  val tagInfo: Array[JmdictTagInfo] = Array(
    JmdictTagInfo(JmdictTag.MA, "MA", """martial arts term"""),
    JmdictTagInfo(JmdictTag.X,
                  "X",
                  """rude or X-rated term (not displayed in educational software)"""),
    JmdictTagInfo(JmdictTag.abbr, "abbr", """abbreviation"""),
    JmdictTagInfo(JmdictTag.adjI, "adj-i", """adjective (keiyoushi)"""),
    JmdictTagInfo(JmdictTag.adjNa,
                  "adj-na",
                  """adjectival nouns or quasi-adjectives (keiyodoshi)"""),
    JmdictTagInfo(JmdictTag.adjNo,
                  "adj-no",
                  """nouns which may take the genitive case particle `no'"""),
    JmdictTagInfo(JmdictTag.adjPn, "adj-pn", """pre-noun adjectival (rentaishi)"""),
    JmdictTagInfo(JmdictTag.adjT, "adj-t", """`taru' adjective"""),
    JmdictTagInfo(JmdictTag.adjF, "adj-f", """noun or verb acting prenominally"""),
    JmdictTagInfo(JmdictTag.adj, "adj", """former adjective classification (being removed)"""),
    JmdictTagInfo(JmdictTag.adv, "adv", """adverb (fukushi)"""),
    JmdictTagInfo(JmdictTag.advTo, "adv-to", """adverb taking the `to' particle"""),
    JmdictTagInfo(JmdictTag.arch, "arch", """archaism"""),
    JmdictTagInfo(JmdictTag.ateji, "ateji", """ateji (phonetic) reading"""),
    JmdictTagInfo(JmdictTag.aux, "aux", """auxiliary"""),
    JmdictTagInfo(JmdictTag.auxV, "aux-v", """auxiliary verb"""),
    JmdictTagInfo(JmdictTag.auxAdj, "aux-adj", """auxiliary adjective"""),
    JmdictTagInfo(JmdictTag.Buddh, "Buddh", """Buddhist term"""),
    JmdictTagInfo(JmdictTag.chem, "chem", """chemistry term"""),
    JmdictTagInfo(JmdictTag.chn, "chn", """children's language"""),
    JmdictTagInfo(JmdictTag.col, "col", """colloquialism"""),
    JmdictTagInfo(JmdictTag.comp, "comp", """computer terminology"""),
    JmdictTagInfo(JmdictTag.conj, "conj", """conjunction"""),
    JmdictTagInfo(JmdictTag.ctr, "ctr", """counter"""),
    JmdictTagInfo(JmdictTag.derog, "derog", """derogatory"""),
    JmdictTagInfo(JmdictTag.exclKanj, "eK", """exclusively kanji"""),
    JmdictTagInfo(JmdictTag.exclKana, "ek", """exclusively kana"""),
    JmdictTagInfo(JmdictTag.exp, "exp", """Expressions (phrases, clauses, etc.)"""),
    JmdictTagInfo(JmdictTag.fam, "fam", """familiar language"""),
    JmdictTagInfo(JmdictTag.fem, "fem", """female term or language"""),
    JmdictTagInfo(JmdictTag.food, "food", """food term"""),
    JmdictTagInfo(JmdictTag.geom, "geom", """geometry term"""),
    JmdictTagInfo(JmdictTag.gikun,
                  "gikun",
                  """gikun (meaning as reading)  or jukujikun (special kanji reading)"""),
    JmdictTagInfo(JmdictTag.hon, "hon", """honorific or respectful (sonkeigo) language"""),
    JmdictTagInfo(JmdictTag.hum, "hum", """humble (kenjougo) language"""),
    JmdictTagInfo(JmdictTag.irrKanji, "iK", """word containing irregular kanji usage"""),
    JmdictTagInfo(JmdictTag.id, "id", """idiomatic expression"""),
    JmdictTagInfo(JmdictTag.irrKana, "ik", """word containing irregular kana usage"""),
    JmdictTagInfo(JmdictTag.interj, "int", """interjection (kandoushi)"""),
    JmdictTagInfo(JmdictTag.io, "io", """irregular okurigana usage"""),
    JmdictTagInfo(JmdictTag.iv, "iv", """irregular verb"""),
    JmdictTagInfo(JmdictTag.ling, "ling", """linguistics terminology"""),
    JmdictTagInfo(JmdictTag.mSl, "m-sl", """manga slang"""),
    JmdictTagInfo(JmdictTag.male, "male", """male term or language"""),
    JmdictTagInfo(JmdictTag.maleSl, "male-sl", """male slang"""),
    JmdictTagInfo(JmdictTag.math, "math", """mathematics"""),
    JmdictTagInfo(JmdictTag.mil, "mil", """military"""),
    JmdictTagInfo(JmdictTag.n, "n", """noun (common) (futsuumeishi)"""),
    JmdictTagInfo(JmdictTag.nAdv, "n-adv", """adverbial noun (fukushitekimeishi)"""),
    JmdictTagInfo(JmdictTag.nSuf, "n-suf", """noun, used as a suffix"""),
    JmdictTagInfo(JmdictTag.nPref, "n-pref", """noun, used as a prefix"""),
    JmdictTagInfo(JmdictTag.nT, "n-t", """noun (temporal) (jisoumeishi)"""),
    JmdictTagInfo(JmdictTag.num, "num", """numeric"""),
    JmdictTagInfo(JmdictTag.outKanji, "oK", """word containing out-dated kanji"""),
    JmdictTagInfo(JmdictTag.obs, "obs", """obsolete term"""),
    JmdictTagInfo(JmdictTag.obsc, "obsc", """obscure term"""),
    JmdictTagInfo(JmdictTag.outKana, "ok", """out-dated or obsolete kana usage"""),
    JmdictTagInfo(JmdictTag.onMim, "on-mim", """onomatopoeic or mimetic word"""),
    JmdictTagInfo(JmdictTag.pn, "pn", """pronoun"""),
    JmdictTagInfo(JmdictTag.poet, "poet", """poetical term"""),
    JmdictTagInfo(JmdictTag.pol, "pol", """polite (teineigo) language"""),
    JmdictTagInfo(JmdictTag.pref, "pref", """prefix"""),
    JmdictTagInfo(JmdictTag.proverb, "proverb", """proverb"""),
    JmdictTagInfo(JmdictTag.prt, "prt", """particle"""),
    JmdictTagInfo(JmdictTag.physics, "physics", """physics terminology"""),
    JmdictTagInfo(JmdictTag.rare, "rare", """rare"""),
    JmdictTagInfo(JmdictTag.sens, "sens", """sensitive"""),
    JmdictTagInfo(JmdictTag.sl, "sl", """slang"""),
    JmdictTagInfo(JmdictTag.suf, "suf", """suffix"""),
    JmdictTagInfo(JmdictTag.useKanji, "uK", """word usually written using kanji alone"""),
    JmdictTagInfo(JmdictTag.useKana, "uk", """word usually written using kana alone"""),
    JmdictTagInfo(JmdictTag.v1, "v1", """Ichidan verb"""),
    JmdictTagInfo(JmdictTag.v2aS, "v2a-s", """Nidan verb with 'u' ending (archaic)"""),
    JmdictTagInfo(JmdictTag.v4h, "v4h", """Yondan verb with `hu/fu' ending (archaic)"""),
    JmdictTagInfo(JmdictTag.v4r, "v4r", """Yondan verb with `ru' ending (archaic)"""),
    JmdictTagInfo(JmdictTag.v5, "v5", """Godan verb (not completely classified)"""),
    JmdictTagInfo(JmdictTag.v5aru, "v5aru", """Godan verb - -aru special class"""),
    JmdictTagInfo(JmdictTag.v5b, "v5b", """Godan verb with `bu' ending"""),
    JmdictTagInfo(JmdictTag.v5g, "v5g", """Godan verb with `gu' ending"""),
    JmdictTagInfo(JmdictTag.v5k, "v5k", """Godan verb with `ku' ending"""),
    JmdictTagInfo(JmdictTag.v5kS, "v5k-s", """Godan verb - Iku/Yuku special class"""),
    JmdictTagInfo(JmdictTag.v5m, "v5m", """Godan verb with `mu' ending"""),
    JmdictTagInfo(JmdictTag.v5n, "v5n", """Godan verb with `nu' ending"""),
    JmdictTagInfo(JmdictTag.v5r, "v5r", """Godan verb with `ru' ending"""),
    JmdictTagInfo(JmdictTag.v5rI, "v5r-i", """Godan verb with `ru' ending (irregular verb)"""),
    JmdictTagInfo(JmdictTag.v5s, "v5s", """Godan verb with `su' ending"""),
    JmdictTagInfo(JmdictTag.v5t, "v5t", """Godan verb with `tsu' ending"""),
    JmdictTagInfo(JmdictTag.v5u, "v5u", """Godan verb with `u' ending"""),
    JmdictTagInfo(JmdictTag.v5uS, "v5u-s", """Godan verb with `u' ending (special class)"""),
    JmdictTagInfo(JmdictTag.v5uru,
                  "v5uru",
                  """Godan verb - Uru old class verb (old form of Eru)"""),
    JmdictTagInfo(JmdictTag.v5z, "v5z", """Godan verb with `zu' ending"""),
    JmdictTagInfo(JmdictTag.vz,
                  "vz",
                  """Ichidan verb - zuru verb (alternative form of -jiru verbs)"""),
    JmdictTagInfo(JmdictTag.vi, "vi", """intransitive verb"""),
    JmdictTagInfo(JmdictTag.vk, "vk", """Kuru verb - special class"""),
    JmdictTagInfo(JmdictTag.vn, "vn", """irregular nu verb"""),
    JmdictTagInfo(JmdictTag.vr, "vr", """irregular ru verb, plain form ends with -ri"""),
    JmdictTagInfo(JmdictTag.vs, "vs", """noun or participle which takes the aux. verb suru"""),
    JmdictTagInfo(JmdictTag.vsC, "vs-c", """su verb - precursor to the modern suru"""),
    JmdictTagInfo(JmdictTag.vsS, "vs-s", """suru verb - special class"""),
    JmdictTagInfo(JmdictTag.vsI, "vs-i", """suru verb - irregular"""),
    JmdictTagInfo(JmdictTag.kyb, "kyb", """Kyoto-ben"""),
    JmdictTagInfo(JmdictTag.osb, "osb", """Osaka-ben"""),
    JmdictTagInfo(JmdictTag.ksb, "ksb", """Kansai-ben"""),
    JmdictTagInfo(JmdictTag.ktb, "ktb", """Kantou-ben"""),
    JmdictTagInfo(JmdictTag.tsb, "tsb", """Tosa-ben"""),
    JmdictTagInfo(JmdictTag.thb, "thb", """Touhoku-ben"""),
    JmdictTagInfo(JmdictTag.tsug, "tsug", """Tsugaru-ben"""),
    JmdictTagInfo(JmdictTag.kyu, "kyu", """Kyuushuu-ben"""),
    JmdictTagInfo(JmdictTag.rkb, "rkb", """Ryuukyuu-ben"""),
    JmdictTagInfo(JmdictTag.nab, "nab", """Nagano-ben"""),
    JmdictTagInfo(JmdictTag.vt, "vt", """transitive verb"""),
    JmdictTagInfo(JmdictTag.vulg, "vulg", """vulgar expression or word""")
  )
}
