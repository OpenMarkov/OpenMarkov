// Bayesian Network
//   Elvira format 

bnet  Unknown { 

// Network Properties

visualprecision = "0.00";
version = 1.0;
default node states = (absent , present);

// Network Variables 

node alcoholism(finite-states) {
title = "alcoholism";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =342;
pos_y =157;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node vh_amn(finite-states) {
title = "vh_amn";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =155;
pos_y =156;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hepatotoxic(finite-states) {
title = "hepatotoxic";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =872;
pos_y =144;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node THepatitis(finite-states) {
title = "THepatitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =812;
pos_y =248;
relevance = 9.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hospital(finite-states) {
title = "hospital";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =143;
pos_y =34;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node surgery(finite-states) {
title = "surgery";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =342;
pos_y =19;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node gallstones(finite-states) {
title = "gallstones";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =648;
pos_y =33;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node choledocholithotomy(finite-states) {
title = "choledocholithotomy";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =503;
pos_y =17;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node injections(finite-states) {
title = "injections";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =294;
pos_y =116;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node transfusion(finite-states) {
title = "transfusion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =201;
pos_y =108;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node ChHepatitis(finite-states) {
title = "ChHepatitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =216;
pos_y =235;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("active" "persist" "absent");
}

node sex(finite-states) {
title = "sex";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =637;
pos_y =167;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("female" "male");
}

node age(finite-states) {
title = "age";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =772;
pos_y =192;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("age65_100" "age51_65" "age31_50" "age0_30");
}

node PBC(finite-states) {
title = "PBC";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =639;
pos_y =255;
relevance = 10.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node fibrosis(finite-states) {
title = "fibrosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =356;
pos_y =195;
relevance = 9.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node diabetes(finite-states) {
title = "diabetes";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =65;
pos_y =144;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node obesity(finite-states) {
title = "obesity";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =66;
pos_y =227;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Steatosis(finite-states) {
title = "Steatosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =86;
pos_y =293;
relevance = 9.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Cirrhosis(finite-states) {
title = "Cirrhosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =449;
pos_y =221;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("decompens" "compens" "absent");
}

node Hyperbilirubinemia(finite-states) {
title = "Hyperbilirubinemia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =749;
pos_y =312;
relevance = 10.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node triglycerides(finite-states) {
title = "triglycerides";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =52;
pos_y =336;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a17_4" "a3_2" "a1_0");
}

node RHepatitis(finite-states) {
title = "RHepatitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =943;
pos_y =209;
relevance = 9.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node fatigue(finite-states) {
title = "fatigue";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =314;
pos_y =418;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node bilirubin(finite-states) {
title = "bilirubin";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =833;
pos_y =396;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a40_20" "a19_8" "a7_2" "a1_0");
}

node itching(finite-states) {
title = "itching";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =783;
pos_y =454;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node upper_pain(finite-states) {
title = "upper_pain";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =625;
pos_y =127;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node fat(finite-states) {
title = "fat";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =518;
pos_y =94;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node pain_ruq(finite-states) {
title = "pain_ruq";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =322;
pos_y =288;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node pressure_ruq(finite-states) {
title = "pressure_ruq";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =438;
pos_y =344;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node phosphatase(finite-states) {
title = "phosphatase";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =605;
pos_y =450;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a7392_1500" "a1499_700" "a699_240" "a239_0");
}

node jaundice(finite-states) {
title = "jaundice";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =931;
pos_y =482;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node ama(finite-states) {
title = "ama";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =730;
pos_y =479;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node le_cells(finite-states) {
title = "le_cells";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =820;
pos_y =103;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node pain(finite-states) {
title = "pain";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =806;
pos_y =592;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node proteins(finite-states) {
title = "proteins";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =660;
pos_y =395;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("a10_6" "a5_2");
}

node edema(finite-states) {
title = "edema";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =377;
pos_y =491;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node platelet(finite-states) {
title = "platelet";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =410;
pos_y =416;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a597_300" "a299_150" "a149_100" "a99_0");
}

node inr(finite-states) {
title = "inr";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =368;
pos_y =373;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a200_110" "a109_70" "a69_0");
}

node bleeding(finite-states) {
title = "bleeding";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =503;
pos_y =466;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node flatulence(finite-states) {
title = "flatulence";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =730;
pos_y =135;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node alcohol(finite-states) {
title = "alcohol";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =400;
pos_y =555;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node encephalopathy(finite-states) {
title = "encephalopathy";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =284;
pos_y =465;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node urea(finite-states) {
title = "urea";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =220;
pos_y =519;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a165_50" "a49_40" "a39_0");
}

node ascites(finite-states) {
title = "ascites";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =664;
pos_y =506;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hepatomegaly(finite-states) {
title = "hepatomegaly";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =868;
pos_y =350;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hepatalgia(finite-states) {
title = "hepatalgia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =947;
pos_y =399;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node density(finite-states) {
title = "density";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =330;
pos_y =531;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node ESR(finite-states) {
title = "ESR";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =507;
pos_y =401;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a200_50" "a49_15" "a14_0");
}

node alt(finite-states) {
title = "alt";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =261;
pos_y =423;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a850_200" "a199_100" "a99_35" "a34_0");
}

node ast(finite-states) {
title = "ast";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =198;
pos_y =412;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a700_400" "a399_150" "a149_40" "a39_0");
}

node amylase(finite-states) {
title = "amylase";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =488;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a1400_500" "a499_300" "a299_0");
}

node ggtp(finite-states) {
title = "ggtp";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =742;
pos_y =396;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("a640_70" "a69_30" "a29_10" "a9_0");
}

node cholesterol(finite-states) {
title = "cholesterol";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =600;
pos_y =342;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a999_350" "a349_240" "a239_0");
}

node hbsag(finite-states) {
title = "hbsag";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =61;
pos_y =461;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hbsag_anti(finite-states) {
title = "hbsag_anti";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =148;
pos_y =438;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node anorexia(finite-states) {
title = "anorexia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =955;
pos_y =303;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node nausea(finite-states) {
title = "nausea";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =974;
pos_y =271;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node spleen(finite-states) {
title = "spleen";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =478;
pos_y =535;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node consciousness(finite-states) {
title = "consciousness";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =263;
pos_y =565;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node spiders(finite-states) {
title = "spiders";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =644;
pos_y =565;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node skin(finite-states) {
title = "skin";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =913;
pos_y =516;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node albumin(finite-states) {
title = "albumin";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =579;
pos_y =398;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("a70_50" "a49_30" "a29_0");
}

node edge(finite-states) {
title = "edge";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =445;
pos_y =490;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node irregular_liver(finite-states) {
title = "irregular_liver";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =562;
pos_y =506;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hbc_anti(finite-states) {
title = "hbc_anti";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =187;
pos_y =493;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hcv_anti(finite-states) {
title = "hcv_anti";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =105;
pos_y =492;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node joints(finite-states) {
title = "joints";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =717;
pos_y =592;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node palms(finite-states) {
title = "palms";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =366;
pos_y =444;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node hbeag(finite-states) {
title = "hbeag";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =46;
pos_y =407;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node jaund_symptoms(finite-states) {
title = "jaund_symptoms";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =807;
pos_y =505;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node carcinoma(finite-states) {
title = "carcinoma";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =499;
pos_y =282;
relevance = 9.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

// links of the associated graph:

link hepatotoxic THepatitis;

link alcoholism THepatitis;

link gallstones choledocholithotomy;

link hospital injections;

link surgery injections;

link choledocholithotomy injections;

link hospital transfusion;

link surgery transfusion;

link choledocholithotomy transfusion;

link transfusion ChHepatitis;

link vh_amn ChHepatitis;

link injections ChHepatitis;

link injections PBC;

link sex PBC;

link age PBC;

link ChHepatitis fibrosis;

link diabetes obesity;

link obesity Steatosis;

link alcoholism Steatosis;

link fibrosis Cirrhosis;

link Steatosis Cirrhosis;

link age Hyperbilirubinemia;

link sex Hyperbilirubinemia;

link Steatosis triglycerides;

link ChHepatitis fatigue;

link THepatitis fatigue;

link RHepatitis fatigue;

link Hyperbilirubinemia bilirubin;

link PBC bilirubin;

link Cirrhosis bilirubin;

link bilirubin itching;

link gallstones upper_pain;

link gallstones fat;

link fat pain_ruq;

link Steatosis pain_ruq;

link Hyperbilirubinemia pain_ruq;

link gallstones pressure_ruq;

link PBC pressure_ruq;

link Cirrhosis pressure_ruq;

link RHepatitis phosphatase;

link THepatitis phosphatase;

link Cirrhosis phosphatase;

link ChHepatitis phosphatase;

link bilirubin jaundice;

link PBC ama;

link PBC le_cells;

link ama pain;

link le_cells pain;

link Cirrhosis proteins;

link Cirrhosis edema;

link Cirrhosis platelet;

link ChHepatitis inr;

link Cirrhosis inr;

link THepatitis inr;

link Hyperbilirubinemia inr;

link platelet bleeding;

link inr bleeding;

link gallstones flatulence;

link Cirrhosis alcohol;

link Cirrhosis encephalopathy;

link encephalopathy urea;

link Cirrhosis ascites;

link hepatotoxic hepatomegaly;

link RHepatitis hepatomegaly;

link THepatitis hepatomegaly;

link Steatosis hepatomegaly;

link Hyperbilirubinemia hepatomegaly;

link hepatomegaly hepatalgia;

link encephalopathy density;

link PBC ESR;

link ChHepatitis ESR;

link Steatosis ESR;

link Hyperbilirubinemia ESR;

link ChHepatitis alt;

link RHepatitis alt;

link THepatitis alt;

link Steatosis alt;

link Cirrhosis alt;

link ChHepatitis ast;

link RHepatitis ast;

link THepatitis ast;

link Steatosis ast;

link Cirrhosis ast;

link gallstones amylase;

link PBC ggtp;

link THepatitis ggtp;

link RHepatitis ggtp;

link Steatosis ggtp;

link ChHepatitis ggtp;

link Hyperbilirubinemia ggtp;

link PBC cholesterol;

link Steatosis cholesterol;

link ChHepatitis cholesterol;

link vh_amn hbsag;

link ChHepatitis hbsag;

link vh_amn hbsag_anti;

link ChHepatitis hbsag_anti;

link RHepatitis anorexia;

link THepatitis anorexia;

link RHepatitis nausea;

link THepatitis nausea;

link Cirrhosis spleen;

link encephalopathy consciousness;

link ascites spiders;

link bleeding spiders;

link bilirubin skin;

link Cirrhosis albumin;

link Cirrhosis edge;

link Cirrhosis irregular_liver;

link vh_amn hbc_anti;

link ChHepatitis hbc_anti;

link vh_amn hcv_anti;

link ChHepatitis hcv_anti;

link ama joints;

link inr palms;

link vh_amn hbeag;

link ChHepatitis hbeag;

link bilirubin jaund_symptoms;

link Cirrhosis carcinoma;

link PBC carcinoma;

//Network Relationships: 

relation alcoholism { 
comment = "new";
deterministic=false;
values= table (0.13590844 0.86409156 );
}

relation vh_amn { 
comment = "new";
deterministic=false;
values= table (0.17310443 0.82689557 );
}

relation hepatotoxic { 
comment = "new";
deterministic=false;
values= table (0.08154506 0.91845494 );
}

relation THepatitis hepatotoxic alcoholism { 
comment = "new";
deterministic=false;
values= table (0.2 0.00191939 0.08888889 0.0326087 0.8 0.99808061 0.91111111 0.9673913 );
}

relation hospital { 
comment = "new";
deterministic=false;
values= table (0.53505007 0.46494993 );
}

relation surgery { 
comment = "new";
deterministic=false;
values= table (0.42346209 0.57653791 );
}

relation gallstones { 
comment = "new";
deterministic=false;
values= table (0.15307582 0.84692418 );
}

relation choledocholithotomy gallstones { 
comment = "new";
deterministic=false;
values= table (0.71028037 0.03716216 0.28971963 0.96283784 );
}

relation injections hospital surgery choledocholithotomy { 
comment = "new";
deterministic=false;
values= table (0.8 0.71584699 0.83333333 0.48181818 0.375 0.23333333 0.01098901 0.0647482 0.2 0.28415301 0.16666667 0.51818182 0.625 0.76666667 0.98901099 0.9352518 );
}

relation transfusion hospital surgery choledocholithotomy { 
comment = "new";
deterministic=false;
values= table (0.33333333 0.28961749 0.16666667 0.11818182 0.125 0.3 0.01098901 0.01079137 0.66666667 0.71038251 0.83333333 0.88181818 0.875 0.7 0.98901099 0.98920863 );
}

relation ChHepatitis transfusion vh_amn injections { 
comment = "new";
deterministic=false;
values= table (0.20942408209424082 0.46153846 0.06 0.13043478 0.1538461515384615 0.24 0.07692308 0.13095238 0.005235600052356001 0.30769231 0.06 0.04347826 0.05128205051282051 0.14 0.00591716 0.05357143 0.7853403178534032 0.23076923 0.88 0.82608696 0.794871797948718 0.62 0.91715976 0.81547619 );
}

relation sex { 
comment = "new";
deterministic=false;
values= table (0.59799714 0.40200286 );
}

relation age { 
comment = "new";
deterministic=false;
values= table (0.0772532192274678 0.387696706123033 0.3977110160228899 0.13733905862660944 );
}

relation PBC injections sex age { 
comment = "new";
deterministic=false;
values= table (0.80952381 0.82105263 0.86075949 0.09090909 0.45454545 0.13043478 0.20833333 0.00763359 0.42857143 0.56097561 0.41052632 0.14285714 0.25 0.07042254 0.025 0.00195695 0.19047619 0.17894737 0.13924051 0.90909091 0.54545455 0.86956522 0.79166667 0.99236641 0.57142857 0.43902439 0.58947368 0.85714286 0.75 0.92957746 0.975 0.99804305 );
}

relation fibrosis ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.4 0.1 0.0010 0.6 0.9 0.999 );
}

relation diabetes { 
comment = "new";
deterministic=false;
values= table (0.03576538 0.96423462 );
}

relation obesity diabetes { 
comment = "new";
deterministic=false;
values= table (0.24 0.06231454 0.76 0.93768546 );
}

relation Steatosis obesity alcoholism { 
comment = "new";
deterministic=false;
values= table (0.36363636 0.18918919 0.23809524 0.06349206 0.63636364 0.81081081 0.76190476 0.93650794 );
}

relation Cirrhosis fibrosis Steatosis { 
comment = "new";
deterministic=false;
values= table (0.35 0.49 0.3 0.0010 0.64 0.49 0.3 0.0010 0.01 0.02 0.4 0.998 );
}

relation Hyperbilirubinemia age sex { 
comment = "new";
deterministic=false;
values= table (0.002849 0.0052356 0.01129944 0.0212766 0.04597701 0.07692308 0.21875 0.453125 0.997151 0.9947644 0.98870056 0.9787234 0.95402299 0.92307692 0.78125 0.546875 );
}

relation triglycerides Steatosis { 
comment = "new";
deterministic=false;
values= table (0.17910448 0.02373418 0.1641791 0.03164557 0.65671642 0.94462025 );
}

relation RHepatitis { 
comment = "new";
deterministic=false;
values= table (0.02432046 0.97567954 );
}

relation fatigue ChHepatitis THepatitis RHepatitis { 
comment = "new";
deterministic=false;
values= table (0.63636364 0.625 0.62365591 0.6043956 0.60714286 0.59322034 0.58928571 0.52777778 0.61538462 0.66666667 0.70588235 0.53598485 0.36363636 0.375 0.37634409 0.3956044 0.39285714 0.40677966 0.41071429 0.47222222 0.38461538 0.33333333 0.29411765 0.46401515 );
}

relation bilirubin Hyperbilirubinemia PBC Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.03076923 0.036585369634146304 0.03614458 0.02762431 0.02255639 0.00177936 0.03571429 0.03968253960317461 0.04642857 0.017241380172413803 0.0321543403215434 0.003649639963503601 0.13846154 0.12804877871951223 0.13654618 0.1160221 0.07518797 0.00177936 0.1377551 0.14682539853174603 0.16785714 0.13793103137931031 0.0032154300321543 0.0109489098905109 0.42307692 0.40853658591463415 0.42168675 0.43646409 0.42857143 0.53380783 0.42346939 0.39682539603174605 0.40357143 0.3965517239655172 0.160771701607717 0.13503649864963502 0.40769231 0.4268292657317074 0.40562249 0.4198895 0.47368421 0.46263345 0.40306122 0.4166666658333334 0.38214286 0.44827586448275863 0.8038585280385853 0.8503649514963505 );
}

relation itching bilirubin { 
comment = "new";
deterministic=false;
values= table (0.875 0.65517241 0.5625 0.33333333 0.125 0.34482759 0.4375 0.66666667 );
}

relation upper_pain gallstones { 
comment = "new";
deterministic=false;
values= table (0.41121495 0.38682432 0.58878505 0.61317568 );
}

relation fat gallstones { 
comment = "new";
deterministic=false;
values= table (0.17757009 0.28040541 0.82242991 0.71959459 );
}

relation pain_ruq fat Steatosis Hyperbilirubinemia { 
comment = "new";
deterministic=false;
values= table (0.61111111 0.66666667 0.5625 0.56081081 0.46153846 0.39130435 0.175 0.37383178 0.38888889 0.33333333 0.4375 0.43918919 0.53846154 0.60869565 0.825 0.62616822 );
}

relation pressure_ruq gallstones PBC Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.3255814 0.33333333 0.32926829 0.2 0.5 0.33333333 0.34328358 0.32777778 0.29292929 0.52830189 0.48275862 0.43910256 0.6744186 0.66666667 0.67073171 0.8 0.5 0.66666667 0.65671642 0.67222222 0.70707071 0.47169811 0.51724138 0.56089744 );
}

relation phosphatase RHepatitis THepatitis Cirrhosis ChHepatitis { 
comment = "new";
kind-of-relation = potential;
deterministic=false;
values= table (0.02083333 0.02173913 0.020833330208333303 0.00191939 0.0024937699750623007 0.0026954199730458003 0.0018148800181488002 0.00226757 0.0023201899767981006 0.014492750144927503 0.01694915 0.01538462 0.00151286 0.00203666 0.002079 0.0013140600131406 0.0015600600156006003 0.005813950058139501 0.016129030161290303 0.016666669833333304 0.02777778 0.013888889861111102 0.0018484299815157005 0.0018148800181488002 0.01176471 0.01351351 0.037037039629629606 0.02298851 0.02247191 0.03448276 0.0112359598876404 0.0012970199870298003 0.0032154300321543 0.01098901 0.00277008 0.0683371306833713 0.02083333 0.02173913 0.020833330208333303 0.01919386 0.024937659750623405 0.026954179730458206 0.0181488201814882 0.02267574 0.023201859767981406 0.014492750144927503 0.01694915 0.01538462 0.01512859 0.0203666 0.02079002 0.013140600131406001 0.015600620156006204 0.005813950058139501 0.016129030161290303 0.016666669833333304 0.02777778 0.027777779722222205 0.036968579630314205 0.036297640362976405 0.02352941 0.02702703 0.037037039629629606 0.02298851 0.02247191 0.03448276 0.03370786966292131 0.03891050961089491 0.0643086806430868 0.02197802 0.02770083 0.1435079714350797 0.29166667 0.30434783 0.3333333333333333 0.26871401 0.24937655750623444 0.26954177730458223 0.21778584217785843 0.20408163 0.18561484814385154 0.2898550728985507 0.3220339 0.38461538 0.30257186 0.28513238 0.31185031 0.2496714824967148 0.23400936234009365 0.23255814232558142 0.2903225829032258 0.31666666683333333 0.34722222 0.27777777722222224 0.2587800374121996 0.272232302722323 0.22352941 0.2027027 0.14814814851851854 0.29885057 0.33707865 0.48275862 0.33707864662921355 0.31128404688715955 0.3858520938585209 0.21978022 0.19390582 0.3394077433940774 0.66666667 0.65217391 0.6250000062500001 0.71017274 0.72319201276808 0.7008086229919138 0.7622504576225045 0.77097506 0.788863102111369 0.6811594268115942 0.6440678 0.58461538 0.68078669 0.69246436 0.66528067 0.7358738573587386 0.7488299574882996 0.7558139575581395 0.6774193567741936 0.6499999935000002 0.59722222 0.6805555531944445 0.7024029529759706 0.6896551768965518 0.74117647 0.75675676 0.7777777722222223 0.65517241 0.61797753 0.44827586 0.6179775238202248 0.6485084235149158 0.546623795466238 0.74725275 0.77562327 0.4487471544874715 );
}

relation jaundice bilirubin { 
comment = "new";
deterministic=false;
values= table (0.75 0.56896552 0.35576923 0.1942446 0.25 0.43103448 0.64423077 0.8057554 );
}

relation ama PBC { 
comment = "new";
deterministic=false;
values= table (0.56785714 0.01193317 0.43214286 0.98806683 );
}

relation le_cells PBC { 
comment = "new";
deterministic=false;
values= table (0.12142857 0.04057279 0.87857143 0.95942721 );
}

relation pain ama le_cells { 
comment = "new";
deterministic=false;
values= table (0.27777778 0.17123288 0.27272727 0.22709163 0.72222222 0.82876712 0.72727273 0.77290837 );
}

relation proteins Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.99827883 0.99678457 0.98032787 0.00172117 0.00321543 0.01967213 );
}

relation edema Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.34482759 0.06451613 0.13114754 0.65517241 0.93548387 0.86885246 );
}

relation platelet Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.06896552 0.06451613 0.08032787000000001 0.46551724 0.64516129 0.7065573800000001 0.27586207 0.16129032 0.14754098000000002 0.18965517 0.12903226 0.06557377000000002 );
}

relation inr ChHepatitis Cirrhosis THepatitis Hyperbilirubinemia { 
comment = "new";
deterministic=false;
values= table (0.01754386 0.01298701 0.02150538 0.01666667 0.025 0.01333333 0.02409639 0.01960784 0.02197802 0.01923077 0.024 0.02197802 0.03030303 0.032258060322580606 0.02898551 0.02469136 0.03508772 0.04081633 0.03571428964285711 0.0333333303333333 0.05084746 0.05 0.05333333 0.08333333 0.03448276 0.01428571 0.02173913 0.00172117 0.0178571401785714 0.01785714 0.028169010281690103 0.00321543 0.03508772 0.037037039629629606 0.05357143 0.065 0.84210526 0.81818182 0.8172043 0.8 0.85 0.86666667 0.86746988 0.85294118 0.89010989 0.90384615 0.904 0.9010989 0.84848485 0.790322587903226 0.79710145 0.75308642 0.8245614 0.83673469 0.8571428514285715 0.8333333383333333 0.88135593 0.9 0.90666667 0.88888889 0.81034483 0.75714286 0.75 0.60240964 0.7678571476785715 0.78571429 0.8028169080281691 0.67524116 0.84210526 0.9259259207407409 0.89285714 0.875 0.14035088 0.16883117 0.16129032 0.18333333 0.125 0.12 0.10843373 0.12745098 0.08791209 0.07692308 0.072 0.07692308 0.12121212 0.17741935177419352 0.17391304 0.22222222 0.14035088 0.12244898 0.10714285892857142 0.1333333313333333 0.06779661 0.05 0.04 0.02777778 0.15517241 0.22857143 0.22826087 0.39586919 0.2142857121428571 0.19642857 0.16901408169014082 0.32154341 0.12280702 0.037037039629629606 0.05357143 0.06 );
}

relation bleeding platelet inr { 
comment = "new";
deterministic=false;
values= table (0.14285714 0.10638298 0.09090909 0.13043478 0.1373494 0.425 0.2 0.13333333 0.25 0.5 0.25581395 0.66666667 0.85714286 0.89361702 0.90909091 0.86956522 0.8626506 0.575 0.8 0.86666667 0.75 0.5 0.74418605 0.33333333 );
}

relation flatulence gallstones { 
comment = "new";
deterministic=false;
values= table (0.39252336 0.43074324 0.60747664 0.56925676 );
}

relation alcohol Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.20689655 0.22580645 0.11147541 0.79310345 0.77419355 0.88852459 );
}

relation encephalopathy Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.05172414 0.00321543 0.03278689 0.94827586 0.99678457 0.96721311 );
}

relation urea encephalopathy { 
comment = "new";
deterministic=false;
values= table (0.21739130217391303 0.03550296 0.1304347813043478 0.06508876 0.6521739165217392 0.89940828 );
}

relation ascites Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.63793103 0.09677419 0.09016393 0.36206897 0.90322581 0.90983607 );
}

relation hepatomegaly hepatotoxic RHepatitis THepatitis Steatosis Hyperbilirubinemia { 
comment = "new";
deterministic=false;
values= table (0.90909091 0.95238095 0.95238095 0.95238095 0.95238095 0.96774194 0.96774194 0.90909091 0.95238095 0.96774194 0.96774194 0.90909091 0.96774194 0.96774194 0.66666667 0.7755102 0.62745098 0.67307692 0.58333333 0.65909091 0.57627119 0.63768116 0.53623188 0.6875 0.58490566 0.66197183 0.57333333 0.76923077 0.57746479 0.671875 0.35849057 0.68944099 0.09090909 0.04761905 0.04761905 0.04761905 0.04761905 0.03225806 0.03225806 0.09090909 0.04761905 0.03225806 0.03225806 0.09090909 0.03225806 0.03225806 0.33333333 0.2244898 0.37254902 0.32692308 0.41666667 0.34090909 0.42372881 0.36231884 0.46376812 0.3125 0.41509434 0.33802817 0.42666667 0.23076923 0.42253521 0.328125 0.64150943 0.31055901 );
}

relation hepatalgia hepatomegaly { 
comment = "new";
deterministic=false;
values= table (0.31422505 0.03070175 0.68577495 0.96929825 );
}

relation density encephalopathy { 
comment = "new";
deterministic=false;
values= table (0.73913043 0.37721893 0.26086957 0.62278107 );
}

relation ESR PBC ChHepatitis Steatosis Hyperbilirubinemia { 
comment = "new";
deterministic=false;
values= table (0.2704918 0.2972973 0.29411765 0.32055749 0.30939227 0.331550803315508 0.33333333 0.368 0.34259259 0.3629893236298933 0.36363636363636365 0.43214285567857147 0.26829268 0.175 0.10457516104575161 0.03296703 0.06024096 0.08602150913978492 0.05434782945652171 0.05555555944444441 0.07594937 0.13432836 0.01785714 0.04733728 0.17213115 0.18378378 0.19117647 0.20557491 0.17679558 0.1711229917112299 0.17204301 0.184 0.1712963 0.17793594177935942 0.18181818181818182 0.21071428789285712 0.17682927 0.1625 0.16339869163398693 0.21978022 0.12048193 0.08602150913978492 0.0760869592391304 0.05555555944444441 0.06329114 0.05970149 0.07142857 0.05325444 0.55737705 0.51891892 0.51470588 0.4738676 0.51381215 0.497326204973262 0.49462366 0.448 0.48611111 0.4590747345907474 0.4545454545454546 0.35714285642857146 0.55487805 0.6625 0.7320261473202614 0.74725275 0.81927711 0.8279569817204302 0.8695652113043479 0.8888888811111113 0.86075949 0.80597015 0.91071429 0.89940828 );
}

relation alt ChHepatitis RHepatitis THepatitis Steatosis Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.058823529411764705 0.05454545054545451 0.04761905 0.06451613 0.07017544 0.07936508 0.0684931506849315 0.05882353 0.06250000000000001 0.075 0.08333333 0.08988764 0.061728399382716015 0.0547945205479452 0.058823529411764705 0.06976744 0.07792208 0.08333333 0.06862745 0.0625 0.064 0.08148148 0.08661417086614172 0.12087912 0.051724140517241404 0.02173913 0.0021692 0.022727270227272705 0.00269542 0.00262467 0.01923077 0.002079 0.0018148800000000003 0.0188679201886792 0.02272727 0.02083333 0.01724138 0.0019193899808061003 0.00166389 0.01724138 0.02040816 0.018181819818181805 0.02777778 0.01492537 0.01190476 0.03409091 0.02631579 0.027777779722222205 0.02 0.00212314 0.0019193899808061003 0.02 0.0024937699750623007 0.002375300023753 0.016666669833333304 0.0017825300178253005 0.00144718 0.028169010281690107 0.01724138 0.00584795 0.018181819818181805 0.0017211700172117001 0.00131406 0.025 0.01470588 0.0036900399630996005 0.02666667 0.01176471 0.00149031 0.06896551931034481 0.03225806 0.0456989204569892 0.15686274843137252 0.16363636163636364 0.15873016 0.17741935 0.19298246 0.19047619 0.1643835616438356 0.17647059 0.17500000000000002 0.1875 0.20833333 0.2247191 0.17283950827160494 0.1643835616438356 0.15294117847058825 0.1627907 0.18181818 0.1875 0.16666667 0.16666667 0.168 0.17777778 0.19685039196850393 0.23076923 0.15517241155172412 0.13043478 0.10845987 0.11363636113636362 0.13477089 0.1312336 0.11538462 0.12474012 0.12704174000000001 0.1320754713207547 0.15909091 0.16666667 0.12068966 0.11516314884836852 0.09983361 0.10344828 0.12244898 0.10909090890909091 0.11111111 0.11940299 0.10714286 0.11363636 0.13157895 0.13888888861111112 0.12 0.12738854 0.11516314884836852 0.12 0.14962593850374065 0.1425178114251781 0.11666666883333332 0.12477718124777183 0.11577424 0.12676056126760563 0.15517241 0.23391813 0.10909090890909091 0.1032702210327022 0.09198423 0.1 0.11764706 0.07380073926199261 0.09333333 0.10588235 0.08941878 0.12068965879310342 0.19354839 0.1747311817473118 0.4117647058823529 0.41818182418181826 0.41269841 0.41935484 0.42105263 0.41269841 0.4246575342465753 0.42647059 0.41250000000000003 0.425 0.43055556 0.41573034 0.4197530858024692 0.4246575342465753 0.4117647058823529 0.41860465 0.41558442 0.40625 0.42156863 0.42708333 0.416 0.42222222 0.42519685425196857 0.3956044 0.3965517239655172 0.41304348 0.39045553 0.40909091409090914 0.40431267 0.36745407 0.40384615 0.41580042 0.39927405000000005 0.41509434415094343 0.40909091 0.375 0.39655172 0.40307101596928985 0.38269551 0.39655172 0.3877551 0.3454545465454546 0.38888889 0.40298507 0.38095238 0.38636364 0.39473684 0.27777777722222224 0.4 0.42462845 0.4222648757773513 0.44 0.448877805511222 0.42755344427553443 0.4499999955000001 0.4634581146345812 0.44862518 0.46478873464788734 0.48275862 0.46783626 0.43636363563636366 0.447504304475043 0.42049934 0.425 0.44117647 0.36900368630996316 0.42666667 0.44705882 0.41728763 0.4655172353448277 0.51612903 0.4274193542741935 0.37254901627450987 0.36363636363636365 0.38095238 0.33870968 0.31578947 0.31746032 0.3424657534246575 0.33823529 0.35000000000000003 0.3125 0.27777778 0.26966292 0.34567900654321 0.3561643835616438 0.37647058623529417 0.34883721 0.32467532 0.32291667 0.34313725 0.34375 0.352 0.31851852 0.2913385829133859 0.25274725 0.3965517239655172 0.43478261 0.4989154 0.4545454545454546 0.45822102 0.49868766 0.46153846 0.45738046 0.47186933000000003 0.4339622643396226 0.40909091 0.4375 0.46551724 0.4798464452015356 0.51580699 0.48275862 0.46938776 0.5272727247272727 0.47222222 0.46268657 0.5 0.46590909 0.44736842 0.5555555544444445 0.46 0.44585987 0.46065258539347415 0.42 0.3990024860099752 0.42755344427553443 0.4166666658333334 0.4099821740998218 0.4341534 0.38028169380281696 0.34482759 0.29239766 0.43636363563636366 0.447504304475043 0.48620237 0.45 0.42647059 0.5535055344649447 0.45333333 0.43529412 0.49180328 0.3448275865517242 0.25806452 0.35215054352150543 );
}

relation ast ChHepatitis RHepatitis THepatitis Steatosis Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.01960784 0.01818182 0.01612903 0.01612903 0.018181820000000005 0.032258060322580606 0.02777778 0.0294117602941176 0.02531646 0.025 0.027777779722222205 0.03370786966292131 0.02469136 0.0273972602739726 0.023809520238095204 0.02352941 0.02597403 0.03125 0.02912621 0.03125 0.03174603 0.036764709632352906 0.03937008 0.05494505054945051 0.01754386 0.00221729 0.00212314 0.00221729 0.00269542 0.0026954200269542006 0.00191939 0.00203666 0.00181488 0.00184843 0.00226757 0.00212314 0.0017211700172117001 0.00191939 0.00166389 0.00172117 0.00203666 0.00181488 0.00142653 0.00149031 0.00120337 0.0113636398863636 0.0131578901315789 0.02777778 0.0019960099800399003 0.00212314 0.00191939 0.0019960099800399003 0.0024937699750623007 0.0024330900000000006 0.0016638900166389 0.00175131 0.0014265300142653002 0.0014064700000000002 0.0017211700172117001 0.0058479500584795 0.00181488 0.00172117 0.00133156 0.00126422 0.00149031 0.0036900399630996005 0.0013315600133156002 0.00116144 0.00149031 0.01724138 0.032258060322580606 0.01075269 0.1372549 0.12727273 0.14516129 0.16129032 0.16363636000000004 0.17741935177419352 0.15277778 0.1470588214705882 0.15189873 0.175 0.18055555819444444 0.20224718797752814 0.16049383 0.136986301369863 0.14285714142857142 0.16470588 0.16883117 0.1875 0.16504854 0.15625 0.15873016 0.17647058823529413 0.18110236 0.23076923230769233 0.14035088 0.0886918 0.08492569 0.11086475 0.08086253 0.08086253080862531 0.09596929 0.0814664 0.0907441 0.11090573 0.09070295 0.10615711 0.1032702210327022 0.07677543 0.08319468 0.10327022 0.0814664 0.0907441 0.09985735 0.08941878 0.08423586 0.10227272897727273 0.0921052609210526 0.11111111 0.09980039900199601 0.08492569 0.07677543 0.09980039900199601 0.07481296925187032 0.07299270000000001 0.09983361099833611 0.08756567 0.0855920108559201 0.11251758000000002 0.1032702210327022 0.11695906116959061 0.0907441 0.06884682 0.0665779 0.08849558 0.07451565 0.07380073926199261 0.0932090509320905 0.08130081 0.07451565 0.13793103 0.06451613064516132 0.22580645 0.47058824 0.49090909 0.46774194 0.48387097 0.5090909100000001 0.4838709748387098 0.48611111 0.500000005 0.48101266 0.5 0.5277777747222222 0.5056179749438203 0.4691358 0.4794520547945205 0.45238095452380955 0.45882353 0.48051948 0.45833333 0.46601942 0.47916667 0.46031746 0.47058823529411764 0.49606299 0.46153846461538467 0.45614035 0.48780488 0.44585987 0.46563193 0.51212938 0.45822102458221026 0.46065259 0.48879837 0.45372051 0.4805915 0.52154195 0.48832272 0.447504304475043 0.46065259 0.41597338 0.4302926 0.46843177 0.41742287 0.44222539 0.46199702 0.433213 0.44318181556818187 0.47368421473684214 0.36111111 0.4590818354091817 0.48832272 0.46065259 0.4790419152095809 0.5486284245137157 0.5109489100000001 0.4991680549916805 0.52539405 0.49928673499286735 0.5203938100000001 0.5851979358519793 0.6432748564327485 0.47186933 0.48192771 0.43941411 0.4551201 0.49180328 0.36900368630996316 0.45272969452729694 0.48780488 0.43219076 0.5 0.6774193567741936 0.46774194 0.37254902 0.36363636 0.37096774 0.33870968 0.30909091000000005 0.3064516130645161 0.33333333 0.3235294132352941 0.34177215 0.3 0.2638888873611111 0.25842696741573035 0.34567901 0.3561643835616438 0.38095238380952384 0.35294118 0.32467532 0.32291667 0.33980583 0.33333333 0.34920635 0.31617646683823536 0.28346457 0.25274725252747254 0.38596491 0.42128603 0.4670913 0.42128603 0.40431267 0.45822102458221026 0.44145873 0.42769857 0.45372051 0.40665434 0.38548753 0.40339703 0.447504304475043 0.46065259 0.49916805 0.46471601 0.44806517 0.49001815 0.45649073 0.44709389 0.48134777 0.44318181556818187 0.4210526342105263 0.5 0.43912175560878247 0.42462845 0.46065259 0.41916167580838326 0.37406483625935166 0.41362530000000003 0.39933444399334445 0.38528897 0.41369472413694724 0.36568214000000004 0.3098106730981067 0.23391813233918132 0.43557169 0.4475043 0.49267643 0.4551201 0.43219076 0.5535055344649447 0.45272969452729694 0.42973287 0.49180328 0.34482759 0.22580645225806453 0.29569892 );
}

relation amylase gallstones { 
comment = "new";
deterministic=false;
values= table (0.01869159 0.01013514 0.04672897 0.01689189 0.93457944 0.97297297 );
}

relation ggtp PBC THepatitis RHepatitis Steatosis ChHepatitis Hyperbilirubinemia { 
comment = "new";
deterministic=false;
values= table (0.1590909115909091 0.16964285830357143 0.15463917845360822 0.17307692173076922 0.16666667 0.18548387185483872 0.16949152830508474 0.18320611 0.17592592824074074 0.19130435 0.18852458811475412 0.21088435 0.16666667 0.17687075000000002 0.16528926 0.17424242 0.17266187 0.18934911 0.17419354825806452 0.18579235 0.17482517 0.19375 0.19186047 0.21428571 0.17567568 0.18 0.16666667 0.17829457 0.17647059 0.19393939 0.17763158 0.18994413189944132 0.17857143 0.19871794801282053 0.19642857 0.22077922 0.17877095 0.1804878 0.16981132169811322 0.18435754 0.18229167 0.19776119 0.18260869817391304 0.19397993 0.1846846818468468 0.20074349 0.19867549801324505 0.23928571 0.1509434 0.10526316 0.05555556 0.06122449 0.05649717943502821 0.07393715 0.06557376934426232 0.078125 0.04166667 0.04761905 0.043383949566160505 0.06651885 0.05714286 0.07142857 0.04545454954545451 0.04615385 0.04285714 0.0617284 0.06024096060240961 0.07070707 0.04166667 0.03030303 0.02702703 0.0738007392619926 0.06349206 0.07594936924050631 0.0483871 0.0500000005 0.04615384953846151 0.06756757 0.06410256064102561 0.07692308 0.04545454954545451 0.034482759655172404 0.03076923 0.11695906116959062 0.06493506064935062 0.07692307923076921 0.04444444044444441 0.04210526 0.03703704 0.07462686925373131 0.05660377 0.08791209 0.04395604 0.00277008 0.00177936 0.08 0.1477272714772727 0.16071428839285715 0.14432989855670103 0.1538461515384615 0.14814815 0.16129032161290321 0.16101694838983052 0.17557252 0.15740740842592593 0.17391304 0.16393442836065572 0.18367347 0.15909091 0.16326531000000002 0.14876033 0.15909091 0.15107914 0.16568047 0.16774193832258064 0.18579235 0.16783217 0.18125 0.1744186 0.19327731 0.16216216 0.16666667 0.15 0.1627907 0.15441176 0.16969697 0.17105263 0.18994413189944132 0.17142857 0.18589743814102563 0.17857143 0.1991342 0.17318436 0.17560976 0.15723270157232702 0.16759777 0.16145833 0.17910448 0.17826086821739132 0.19397993 0.1846846818468468 0.19702602 0.19205297807947025 0.225 0.14150943 0.09210526 0.03703704 0.02040816 0.0018832399811676004 0.00184843 0.04918032950819671 0.078125 0.02083333 0.02380952 0.0021691999783080003 0.00221729 0.04285714 0.07142857 0.030303029696969706 0.03076923 0.01428571 0.01234568 0.04819277048192771 0.08080808 0.04166667 0.03030303 0.01351351 0.0036900399630995996 0.04761905 0.07594936924050631 0.03225806 0.0333333303333333 0.015384619846153802 0.01351351 0.051282050512820504 0.08791209 0.04545454954545451 0.034482759655172404 0.01538462 0.005847950058479501 0.06493506064935062 0.08547008914529912 0.04444444044444441 0.04210526 0.02777778 0.029850749701492502 0.06603774 0.14285714 0.07692308 0.05540166 0.00177936 0.096 0.1136363611363636 0.12499999875000002 0.11340205886597941 0.12500000125000002 0.11111111 0.12096774120967742 0.11864406881355932 0.12977099 0.11111110888888892 0.12173913 0.10655737893442621 0.11564626 0.12121212 0.12925170000000002 0.11570248 0.12878788 0.11510791 0.12426036 0.12258064877419352 0.13114754 0.11888112 0.125 0.11046512 0.11764706 0.12162162 0.13333333 0.125 0.13178295 0.11764706 0.12727273 0.125 0.13407821134078213 0.12142857 0.12820512871794873 0.11309524 0.12121212 0.12290503 0.13658537 0.12578616125786163 0.13407821 0.11979167 0.12686567 0.12608695873913042 0.13712375 0.12612613126126132 0.133829 0.12251655877483442 0.13214286 0.12264151 0.13157895 0.09259259 0.10204082 0.07532956924670431 0.09242144 0.09836065901639342 0.125 0.08333333 0.0952381 0.043383949566160505 0.0443459 0.1 0.13095238 0.10606060893939391 0.12307692 0.08571429 0.09876543 0.09638554096385542 0.12121212 0.09722222 0.10606061 0.05405405 0.0369003696309963 0.11111111 0.13924050860759493 0.11290323 0.1333333313333333 0.09230768907692312 0.10810811 0.10256410102564101 0.13186813 0.10606060893939391 0.12068965879310342 0.06153846 0.05847953058479531 0.1168831211688312 0.1452991485470085 0.12222222122222222 0.13684211 0.10185185 0.13432835865671644 0.12264151 0.17582418 0.13186813 0.19390582 0.01779359 0.144 0.5795454557954546 0.5446428545535714 0.5876288641237114 0.5480769254807693 0.57407407 0.5322580653225807 0.5508474544915254 0.51145038 0.5555555544444445 0.51304348 0.5409836045901639 0.48979592 0.5530303 0.5306122400000001 0.57024793 0.53787879 0.56115108 0.52071006 0.5354838646451614 0.49726776 0.53846154 0.5 0.52325581 0.47478992 0.54054054 0.52 0.55833333 0.52713178 0.55147059 0.50909091 0.52631579 0.48603352486033524 0.52857143 0.4871794851282052 0.51190476 0.45887446 0.52513966 0.50731707 0.5471698154716982 0.51396648 0.53645833 0.49626866 0.5130434748695653 0.47491639 0.5045045050450451 0.46840149 0.48675496513245037 0.40357143 0.58490566 0.67105263 0.81481481 0.81632653 0.8662900113370999 0.83179298 0.7868852421311476 0.71875 0.85416667 0.83333333 0.911062900889371 0.88691796 0.8 0.72619048 0.8181818118181818 0.8 0.85714286 0.82716049 0.7951807279518073 0.72727273 0.81944444 0.83333333 0.90540541 0.8856088511439114 0.77777778 0.7088607529113925 0.80645161 0.7833333378333334 0.8461538415384616 0.81081081 0.7820512878205128 0.7032967 0.8030302919696971 0.8103448218965519 0.89230769 0.8187134581871346 0.7532467575324676 0.6923076830769233 0.7888888978888889 0.77894737 0.83333333 0.7611940223880598 0.75471698 0.59340659 0.74725275 0.74792244 0.97864769 0.68 );
}

relation cholesterol PBC Steatosis ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.08965517 0.09659091 0.10344827896551723 0.10158730101587302 0.10509554105095541 0.125 0.09174312 0.06918239 0.04477612 0.03296703 0.00277008 4.4425E-4 0.28275862 0.30113636 0.325670496743295 0.304761903047619 0.3152866231528662 0.36428571 0.27981651 0.23899371 0.2238806 0.06593407 0.02770083 0.09773434 0.62758621 0.60227273 0.5708812242911878 0.5936507959365079 0.5796178357961783 0.51071429 0.62844037 0.6918239 0.73134328 0.9010989 0.96952909 0.90182141 );
}

relation hbsag vh_amn ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.5 0.46153846 0.1125 0.19047619 0.04347826 0.04674797 0.5 0.53846154 0.8875 0.80952381 0.95652174 0.95325203 );
}

relation hbsag_anti vh_amn ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.03571429 0.00763359 0.0375 0.01587302 0.004329 0.01422764 0.96428571 0.99236641 0.9625 0.98412698 0.995671 0.98577236 );
}

relation anorexia RHepatitis THepatitis { 
comment = "new";
deterministic=false;
values= table (0.18181818 0.11764706 0.22222222 0.28091603 0.81818182 0.88235294 0.77777778 0.71908397 );
}

relation nausea RHepatitis THepatitis { 
comment = "new";
deterministic=false;
values= table (0.36363636 0.35294118 0.37037037 0.28549618 0.63636364 0.64705882 0.62962963 0.71450382 );
}

relation spleen Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.48275862 0.25806452 0.10163934 0.51724138 0.74193548 0.89836066 );
}

relation consciousness encephalopathy { 
comment = "new";
deterministic=false;
values= table (0.30434783 0.01627219 0.69565217 0.98372781 );
}

relation spiders ascites bleeding { 
comment = "new";
deterministic=false;
values= table (0.62962963 0.35294118 0.29347826 0.18359375 0.37037037 0.64705882 0.70652174 0.81640625 );
}

relation skin bilirubin { 
comment = "new";
deterministic=false;
values= table (0.99378882 0.87931034 0.71634615 0.1822542 0.00621118 0.12068966 0.28365385 0.8177458 );
}

relation albumin Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.91222031 0.96463023 0.73934426 0.08605852 0.00321543 0.14262295 0.00172117 0.03215434 0.11803279 );
}

relation edge Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.75862069 0.4516129 0.23442623 0.24137931 0.5483871 0.76557377 );
}

relation irregular_liver Cirrhosis { 
comment = "new";
deterministic=false;
values= table (0.60344828 0.35483871 0.10655738 0.39655172 0.64516129 0.89344262 );
}

relation hbc_anti vh_amn ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.00355872 0.00763359 0.0875 0.07936508 0.13043478 0.10162602 0.99644128 0.99236641 0.9125 0.92063492 0.86956522 0.89837398 );
}

relation hcv_anti vh_amn ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.00355872 0.00763359 0.00124844 0.00158479 0.004329 0.00203252 0.99644128 0.99236641 0.99875156 0.99841521 0.995671 0.99796748 );
}

relation joints ama { 
comment = "new";
deterministic=false;
values= table (0.10365854 0.11401869 0.89634146 0.88598131 );
}

relation palms inr { 
comment = "new";
deterministic=false;
values= table (0.14285714 0.15630252 0.37681159 0.85714286 0.84369748 0.62318841 );
}

relation hbeag vh_amn ChHepatitis { 
comment = "new";
deterministic=false;
values= table (0.00355872 0.00763359 0.00124844 0.00158479 0.04347826 0.00203252 0.99644128 0.99236641 0.99875156 0.99841521 0.95652174 0.99796748 );
}

relation jaund_symptoms bilirubin { 
comment = "new";
deterministic=false;
values= table (0.875 0.55172414 0.63461538 0.2853717 0.125 0.44827586 0.36538462 0.7146283 );
}

relation carcinoma Cirrhosis PBC { 
comment = "new";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.3 0.4 0.4 0.1 0.1 0.7 0.7 0.6 0.6 0.9 0.9 );
}

}
