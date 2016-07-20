
package ws.kotonoha.akane.dic.jmdict


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
}

