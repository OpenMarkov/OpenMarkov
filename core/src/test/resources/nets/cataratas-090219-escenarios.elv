// Bayesian Network
//   Elvira format 

bnet  "" { 

// Network Properties

kindofgraph = "mixed";
visualprecision = "0.000000";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node av_sin_catar(finite-states) {
comment = "Disminución agudeza por causas distintas de la catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =723;
pos_y =157;
relevance = 9.0;
purpose = "The rank of values is: [0.0,1.0]";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node camara_estrecha(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =75;
pos_y =143;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node ojo_hundido(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =224;
pos_y =144;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node miopia_magna(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =328;
pos_y =166;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node pupila_estrecha(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =232;
pos_y =275;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node pseudoexfoliacion(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =140;
pos_y =318;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node tipo_catarata(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =502;
pos_y =93;
relevance = 10.0;
purpose = "";
num-states = 5;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve");
}

node ojo_vitrectomizado(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =311;
pos_y =327;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node mala_colaboracion(finite-states) {
comment = "Prevista";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =360;
pos_y =386;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node retinopatia_diabetica(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =118;
pos_y =38;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("proliferativa" "no proliferativa" "ausente");
}

node retinopatia_nd(finite-states) {
comment = "No diabetica";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =265;
pos_y =87;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node maculopatias(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =347;
pos_y =28;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node neuropatias(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =737;
pos_y =30;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node ambliopia(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =580;
pos_y =21;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node opac_corneales(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1072;
pos_y =35;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node distrofia_fuchs(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =878;
pos_y =29;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node av_complic(finite-states) {
comment = "largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =88;
pos_y =726;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node incision_anormal(finite-states) {
comment = "perioperatoria";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =67;
pos_y =643;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node endoftalmitis(finite-states) {
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =248;
pos_y =622;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node edema_corneal(finite-states) {
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =488;
pos_y =636;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node edema_mac_cist(finite-states) {
comment = "clínico, a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =332;
pos_y =665;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node mecha_vitrea(finite-states) {
comment = "perioperatoria";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =340;
pos_y =578;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node ruptura_caps_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =105;
pos_y =575;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node av_pre(finite-states) {
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =925;
pos_y =279;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node av_post(finite-states) {
comment = "Agudeza visual  post-intervención,  corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =212;
pos_y =740;
relevance = 6.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node fvnd_pre_catar(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =760;
pos_y =479;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node otros_trast_fv(finite-states) {
comment = "Otros trastornos (distintos pérdida agudeza y deslu) no debidos a cataratas: brillo, contraste, campo, color, 3D...";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =935;
pos_y =473;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =849;
pos_y =553;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node otros_trast_fvnd_complic(finite-states) {
comment = "Otros trastornos FV largo plazo (distintos agudeza y deslu) debidos a complicaciones operacion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =462;
pos_y =708;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-problem");
}

node fvnd_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =764;
pos_y =688;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-problem");
}

node av_contral(finite-states) {
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1077;
pos_y =319;
relevance = 5.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node fvnd_contral(finite-states) {
comment = "Otros trastornos (distintos pérdida agudeza) no debidos a cataratas: brillo, contraste, campo, color, 3D...";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1070;
pos_y =441;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1061;
pos_y =609;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =942;
pos_y =703;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node despr_retina(finite-states) {
comment = "largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =547;
pos_y =591;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node fibrosis_c_ant(finite-states) {
comment = "Fibrosis de capsula 
anterior";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =291;
pos_y =472;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node sinequias_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =389;
pos_y =199;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node sublux_cristalino(finite-states) {
comment = "subluxación del cristalino";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =129;
pos_y =448;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node despr_coroideo(finite-states) {
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =194;
pos_y =679;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_complic(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =624;
pos_y =677;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_pre_no_catar(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =714;
pos_y =406;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_contral(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =994;
pos_y =535;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_catar(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =537;
pos_y =333;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =573;
pos_y =466;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =670;
pos_y =737;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node fv_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =855;
pos_y =750;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("limit-diaria" "limit-ocio" "sin-limit");
}

node fv_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1097;
pos_y =675;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "limit-ocio" "sin-limit");
}

node catarata_contral(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1102;
pos_y =182;
relevance = 7.0;
purpose = "";
num-states = 6;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve" "ausente");
}

node deslu_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =731;
pos_y =609;
relevance = 5.0;
purpose = "";
num-states = 5;
states = ("ojo operar" "ojo contral" "ambos" "no sabe" "ausente");
}

node deslu_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =732;
pos_y =787;
relevance = 6.0;
purpose = "";
num-states = 5;
states = ("ojo operar" "ojo contral" "ambos" "no sabe" "ausente");
}

node contrala_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1117;
pos_y =380;
relevance = 4.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node agudepre_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =920;
pos_y =356;
relevance = 4.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node agudepos_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =681;
pos_y =284;
relevance = 4.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node patolo_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1123;
pos_y =84;
relevance = 4.0;
purpose = "";
num-states = 5;
states = ("asoc distrof corn" "imposible" "asoc otra patol" "asoc retinop diab" "catarata simple");
}

node laterali_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1133;
pos_y =493;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("bilateral" "unilateral");
}

node comtec_baja_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =72;
pos_y =253;
relevance = 4.0;
purpose = "";
num-states = 2;
states = (">2 leves" "0-1 leves");
}

node comtec_med_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =81;
pos_y =386;
relevance = 4.0;
purpose = "";
num-states = 3;
states = (">2 mod" "1 mod" "ninguna mod");
}

node comtec_alta_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =81;
pos_y =513;
relevance = 4.0;
purpose = "";
num-states = 2;
states = ("alta" "nula/baja/mod");
}

node funcion_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1091;
pos_y =737;
relevance = 4.0;
purpose = "";
num-states = 4;
states = ("limit-diaria" "limit-ocio" "deslu" "sin-limit");
}

node funcion_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1009;
pos_y =787;
relevance = 9.0;
purpose = "";
num-states = 4;
states = ("limit-diaria" "limit-ocio" "deslu" "sin-limit");
}

node ganancia_av(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =356;
pos_y =784;
relevance = 10.0;
purpose = "";
num-states = 7;
states = ("g+3" "g+2" "g+1" "g0" "g-1" "g-2" "g-3");
}

node ganancia_deslu(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =561;
pos_y =779;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("g+1" "g0" "g-1");
}

// Links of the associated graph:

link ambliopia av_sin_catar;

link ambliopia patolo_RAND;

link av_complic av_post;

link av_contral contrala_RAND;

link av_contral fvnd_contral;

link av_post fvnd_post;

link av_post ganancia_av;

link av_pre agudepre_RAND;

link av_pre fvnd_pre_catar;

link av_pre ganancia_av;

link av_sin_catar agudepos_RAND;

link av_sin_catar av_post;

link av_sin_catar av_pre;

link camara_estrecha comtec_baja_RAND;

link camara_estrecha despr_coroideo;

link camara_estrecha edema_corneal;

link camara_estrecha incision_anormal;

link camara_estrecha ruptura_caps_post;

link catarata_contral av_contral;

link catarata_contral deslu_contral;

link catarata_contral laterali_RAND;

link comtec_baja_RAND comtec_med_RAND;

link comtec_med_RAND comtec_alta_RAND;

link deslu_catar deslu_pre;

link deslu_complic deslu_post;

link deslu_contral deslu_global_post;

link deslu_contral deslu_global_pre;

link deslu_contral fv_global_post;

link deslu_contral fv_global_pre;

link deslu_global_post funcion_post;

link deslu_global_pre funcion_RAND;

link deslu_post deslu_global_post;

link deslu_post fv_global_post;

link deslu_post ganancia_deslu;

link deslu_pre deslu_global_pre;

link deslu_pre fv_global_pre;

link deslu_pre ganancia_deslu;

link deslu_pre_no_catar deslu_post;

link deslu_pre_no_catar deslu_pre;

link despr_coroideo av_complic;

link despr_coroideo otros_trast_fvnd_complic;

link despr_retina av_complic;

link despr_retina otros_trast_fvnd_complic;

link distrofia_fuchs av_sin_catar;

link distrofia_fuchs deslu_pre_no_catar;

link distrofia_fuchs edema_corneal;

link distrofia_fuchs opac_corneales;

link distrofia_fuchs otros_trast_fv;

link distrofia_fuchs patolo_RAND;

link edema_corneal av_complic;

link edema_corneal deslu_complic;

link edema_corneal otros_trast_fvnd_complic;

link edema_mac_cist av_complic;

link edema_mac_cist deslu_complic;

link edema_mac_cist otros_trast_fvnd_complic;

link endoftalmitis av_complic;

link endoftalmitis edema_corneal;

link endoftalmitis edema_mac_cist;

link fibrosis_c_ant comtec_alta_RAND;

link fibrosis_c_ant ruptura_caps_post;

link fv_global_post funcion_post;

link fv_global_pre funcion_RAND;

link fvnd_contral fvnd_global_post;

link fvnd_contral fvnd_global_pre;

link fvnd_global_post fv_global_post;

link fvnd_global_pre fv_global_pre;

link fvnd_post fvnd_global_post;

link fvnd_pre fvnd_global_pre;

link fvnd_pre_catar fvnd_pre;

link incision_anormal despr_coroideo;

link incision_anormal endoftalmitis;

link maculopatias av_sin_catar;

link maculopatias deslu_pre_no_catar;

link maculopatias otros_trast_fv;

link maculopatias patolo_RAND;

link mala_colaboracion comtec_med_RAND;

link mala_colaboracion despr_coroideo;

link mala_colaboracion incision_anormal;

link mala_colaboracion ruptura_caps_post;

link mecha_vitrea despr_retina;

link mecha_vitrea edema_mac_cist;

link mecha_vitrea endoftalmitis;

link miopia_magna comtec_baja_RAND;

link miopia_magna deslu_pre_no_catar;

link miopia_magna despr_retina;

link miopia_magna incision_anormal;

link miopia_magna mecha_vitrea;

link neuropatias av_sin_catar;

link neuropatias patolo_RAND;

link ojo_hundido comtec_baja_RAND;

link ojo_hundido edema_corneal;

link ojo_hundido incision_anormal;

link ojo_hundido ruptura_caps_post;

link ojo_vitrectomizado comtec_med_RAND;

link ojo_vitrectomizado pupila_estrecha;

link ojo_vitrectomizado ruptura_caps_post;

link opac_corneales av_sin_catar;

link opac_corneales deslu_pre_no_catar;

link opac_corneales otros_trast_fv;

link opac_corneales patolo_RAND;

link otros_trast_fv fvnd_post;

link otros_trast_fv fvnd_pre;

link otros_trast_fvnd_complic fvnd_post;

link pseudoexfoliacion comtec_med_RAND;

link pseudoexfoliacion edema_mac_cist;

link pseudoexfoliacion incision_anormal;

link pseudoexfoliacion mecha_vitrea;

link pseudoexfoliacion pupila_estrecha;

link pseudoexfoliacion ruptura_caps_post;

link pupila_estrecha comtec_baja_RAND;

link pupila_estrecha incision_anormal;

link pupila_estrecha mecha_vitrea;

link pupila_estrecha ruptura_caps_post;

link retinopatia_diabetica av_sin_catar;

link retinopatia_diabetica edema_mac_cist;

link retinopatia_diabetica maculopatias;

link retinopatia_diabetica otros_trast_fv;

link retinopatia_diabetica patolo_RAND;

link retinopatia_diabetica pupila_estrecha;

link retinopatia_nd av_sin_catar;

link retinopatia_nd despr_retina;

link retinopatia_nd edema_mac_cist;

link retinopatia_nd otros_trast_fv;

link retinopatia_nd patolo_RAND;

link ruptura_caps_post despr_coroideo;

link ruptura_caps_post edema_mac_cist;

link ruptura_caps_post endoftalmitis;

link ruptura_caps_post mecha_vitrea;

link sinequias_post comtec_baja_RAND;

link sinequias_post pupila_estrecha;

link sublux_cristalino comtec_alta_RAND;

link sublux_cristalino mecha_vitrea;

link tipo_catarata av_pre;

link tipo_catarata comtec_alta_RAND;

link tipo_catarata comtec_med_RAND;

link tipo_catarata deslu_catar;

link tipo_catarata edema_corneal;

link tipo_catarata fvnd_pre_catar;

link tipo_catarata incision_anormal;

link tipo_catarata ruptura_caps_post;

//Network Relationships: 

relation camara_estrecha { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.98 );
}

relation ojo_hundido { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.98 );
}

relation miopia_magna { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.05 0.95 );
}

relation pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
}

relation ojo_vitrectomizado { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.03 0.97 );
}

relation mala_colaboracion { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.03 0.97 );
}

relation retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0015 0.028 0.9705 );
}

relation neuropatias { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.03 0.97 );
}

relation distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0015 0.9985 );
}

relation retinopatia_nd { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.043 0.957 );
}

relation ambliopia { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.029 0.971 );
}

relation opac_corneales distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.4 0.01 0.6 0.99 );
}

relation av_pre av_sin_catar tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          Min(av_preav_sin_catar,av_pretipo_catarata,av_preResidual);

}

relation maculopatias retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.3 0.08 0.3 0.7 0.92 );
}

relation av_pre av_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_preav_sin_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation av_pre tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_pretipo_catarata;
deterministic=false;
values= table (0.1 0.01 0.01 0.2 0.999 0.25 0.15 0.01 0.6 0.0010 0.4 0.3 0.3 0.19 0.0 0.25 0.54 0.68 0.01 0.0 );
}

relation av_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 );
}

relation tipo_catarata { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.03 0.03 0.8 0.12 );
}

relation edema_corneal camara_estrecha distrofia_fuchs endoftalmitis ojo_hundido tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          CausalMax(edema_cornealcamara_estrecha,edema_cornealdistrofia_fuchs,edema_cornealendoftalmitis,edema_cornealojo_hundido,edema_cornealtipo_catarata,edema_cornealResidual);

henrionVSdiez = "Diez";
}

relation edema_corneal camara_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealcamara_estrecha;
deterministic=false;
values= table (0.0010 0.0 0.999 1.0 );
}

relation edema_corneal distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealdistrofia_fuchs;
deterministic=false;
values= table (0.25 0.0 0.75 1.0 );
}

relation edema_corneal endoftalmitis { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealendoftalmitis;
deterministic=false;
values= table (0.8 0.0 0.2 1.0 );
}

relation edema_corneal ojo_hundido { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealojo_hundido;
deterministic=false;
values= table (0.05 0.0 0.95 1.0 );
}

relation edema_corneal tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealtipo_catarata;
deterministic=false;
values= table (1.0E-4 0.04 0.04 1.0E-4 0.0 0.9999 0.96 0.96 0.9999 1.0 );
}

relation edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealResidual;
deterministic=false;
values= table (1.0E-5 0.99999 );
}

relation av_post av_sin_catar av_complic { 
comment = "";
deterministic=true;
values= function  
          Min(av_postav_sin_catar,av_postav_complic,av_postResidual);

}

relation av_post av_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_postav_sin_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation av_post av_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_postav_complic;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation av_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_postResidual;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 );
}

relation fvnd_pre otros_trast_fv fvnd_pre_catar { 
comment = "";
deterministic=true;
values= function  
          CausalMax(fvnd_preotros_trast_fv,fvnd_prefvnd_pre_catar,fvnd_preResidual);

henrionVSdiez = "Diez";
}

relation av_sin_catar ambliopia distrofia_fuchs maculopatias neuropatias opac_corneales retinopatia_diabetica retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          Min(av_sin_catarambliopia,av_sin_catardistrofia_fuchs,av_sin_catarmaculopatias,av_sin_catarneuropatias,av_sin_cataropac_corneales,av_sin_catarretinopatia_diabetica,av_sin_catarretinopatia_nd,av_sin_catarResidual);

}

relation fvnd_pre_catar av_pre tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(fvnd_pre_catarav_pre,fvnd_pre_catartipo_catarata,fvnd_pre_catarResidual);

}

relation fvnd_pre otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_preotros_trast_fv;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_pre fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_prefvnd_pre_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_preResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation fvnd_contral av_contral { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(fvnd_contralav_contral,fvnd_contralResidual);

}

relation incision_anormal camara_estrecha mala_colaboracion miopia_magna ojo_hundido pseudoexfoliacion pupila_estrecha tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          CausalMax(incision_anormalcamara_estrecha,incision_anormalmala_colaboracion,incision_anormalmiopia_magna,incision_anormalojo_hundido,incision_anormalpseudoexfoliacion,incision_anormalpupila_estrecha,incision_anormaltipo_catarata,incision_anormalResidual);

henrionVSdiez = "Diez";
}

relation endoftalmitis incision_anormal mecha_vitrea ruptura_caps_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(endoftalmitisincision_anormal,endoftalmitismecha_vitrea,endoftalmitisruptura_caps_post,endoftalmitisResidual);

henrionVSdiez = "Diez";
}

relation fibrosis_c_ant { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0030 0.997 );
}

relation ruptura_caps_post fibrosis_c_ant camara_estrecha mala_colaboracion ojo_hundido ojo_vitrectomizado pseudoexfoliacion pupila_estrecha tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          CausalMax(ruptura_caps_postfibrosis_c_ant,ruptura_caps_postcamara_estrecha,ruptura_caps_postmala_colaboracion,ruptura_caps_postojo_hundido,ruptura_caps_postojo_vitrectomizado,ruptura_caps_postpseudoexfoliacion,ruptura_caps_postpupila_estrecha,ruptura_caps_posttipo_catarata,ruptura_caps_postResidual);

henrionVSdiez = "Diez";
}

relation sinequias_post { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0080 0.992 );
}

relation pupila_estrecha ojo_vitrectomizado pseudoexfoliacion retinopatia_diabetica sinequias_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(pupila_estrechaojo_vitrectomizado,pupila_estrechapseudoexfoliacion,pupila_estrecharetinopatia_diabetica,pupila_estrechasinequias_post,pupila_estrechaResidual);

henrionVSdiez = "Diez";
}

relation sublux_cristalino { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0010 0.999 );
}

relation mecha_vitrea miopia_magna pseudoexfoliacion pupila_estrecha ruptura_caps_post sublux_cristalino { 
comment = "";
deterministic=false;
values= function  
          Or(mecha_vitreamiopia_magna,mecha_vitreapseudoexfoliacion,mecha_vitreapupila_estrecha,mecha_vitrearuptura_caps_post,mecha_vitreasublux_cristalino,mecha_vitreaResidual);

henrionVSdiez = "Diez";
}

relation despr_retina mecha_vitrea miopia_magna retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          CausalMax(despr_retinamecha_vitrea,despr_retinamiopia_magna,despr_retinaretinopatia_nd,despr_retinaResidual);

henrionVSdiez = "Diez";
}

relation mecha_vitrea miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreamiopia_magna;
deterministic=false;
values= table (0.05 0.0 0.95 1.0 );
}

relation mecha_vitrea pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreapseudoexfoliacion;
deterministic=false;
values= table (0.03 0.0 0.97 1.0 );
}

relation mecha_vitrea pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreapupila_estrecha;
deterministic=false;
values= table (0.02 0.0 0.98 1.0 );
}

relation mecha_vitrea ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitrearuptura_caps_post;
deterministic=false;
values= table (0.65 0.0 0.35 1.0 );
}

relation mecha_vitrea sublux_cristalino { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreasublux_cristalino;
deterministic=false;
values= table (0.45 0.0 0.55 1.0 );
}

relation mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreaResidual;
deterministic=false;
values= table (0.0020 0.998 );
}

relation mecha_vitrea miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreamiopia_magna;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation mecha_vitrea pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreapseudoexfoliacion;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation mecha_vitrea pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreapupila_estrecha;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation mecha_vitrea ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitrearuptura_caps_post;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation mecha_vitrea sublux_cristalino { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreasublux_cristalino;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreaResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation incision_anormal camara_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalcamara_estrecha;
deterministic=false;
values= table (0.05 0.0 0.95 1.0 );
}

relation incision_anormal mala_colaboracion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalmala_colaboracion;
deterministic=false;
values= table (0.08 0.0 0.92 1.0 );
}

relation incision_anormal miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalmiopia_magna;
deterministic=false;
values= table (0.05 0.0 0.95 1.0 );
}

relation incision_anormal ojo_hundido { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalojo_hundido;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation incision_anormal pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalpseudoexfoliacion;
deterministic=false;
values= table (0.04 0.0 0.96 1.0 );
}

relation incision_anormal pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalpupila_estrecha;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation incision_anormal tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormaltipo_catarata;
deterministic=false;
values= table (0.0 0.12 0.11 0.01 0.0 1.0 0.88 0.89 0.99 1.0 );
}

relation incision_anormal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation ruptura_caps_post fibrosis_c_ant { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postfibrosis_c_ant;
deterministic=false;
values= table (0.2 0.0 0.8 1.0 );
}

relation ruptura_caps_post camara_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postcamara_estrecha;
deterministic=false;
values= table (0.02 0.0 0.98 1.0 );
}

relation ruptura_caps_post mala_colaboracion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postmala_colaboracion;
deterministic=false;
values= table (0.2 0.0 0.8 1.0 );
}

relation ruptura_caps_post ojo_hundido { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postojo_hundido;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation ruptura_caps_post ojo_vitrectomizado { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postojo_vitrectomizado;
deterministic=false;
values= table (0.09 0.0 0.91 1.0 );
}

relation ruptura_caps_post pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postpseudoexfoliacion;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation ruptura_caps_post pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postpupila_estrecha;
deterministic=false;
values= table (0.2 0.0 0.8 1.0 );
}

relation ruptura_caps_post tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_posttipo_catarata;
deterministic=false;
values= table (0.5 0.2 0.15 0.04 0.0 0.5 0.8 0.85 0.96 1.0 );
}

relation ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postResidual;
deterministic=false;
values= table (0.0090 0.991 );
}

relation fvnd_pre_catar av_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarav_pre;
deterministic=false;
values= table (0.0050 0.1 0.3 0.95 0.05 0.2 0.4 0.05 0.945 0.7 0.3 0.0 );
}

relation fvnd_pre_catar tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catartipo_catarata;
deterministic=false;
values= table (0.0010 0.15 0.15 0.0 0.0 0.0020 0.4 0.4 0.05 0.0 0.997 0.45 0.45 0.95 1.0 );
}

relation fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation av_sin_catar ambliopia { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarambliopia;
deterministic=false;
values= table (0.15 1.0 0.65 0.0 0.15 0.0 0.05 0.0 );
}

relation av_sin_catar distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catardistrofia_fuchs;
deterministic=false;
values= table (0.1 1.0 0.4 0.0 0.4 0.0 0.1 0.0 );
}

relation av_sin_catar maculopatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarmaculopatias;
deterministic=false;
values= table (0.01 1.0 0.04 0.0 0.8 0.0 0.15 0.0 );
}

relation av_sin_catar neuropatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarneuropatias;
deterministic=false;
values= table (0.05 1.0 0.1 0.0 0.5 0.0 0.35 0.0 );
}

relation av_sin_catar opac_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_cataropac_corneales;
deterministic=false;
values= table (0.3 1.0 0.6 0.0 0.05 0.0 0.05 0.0 );
}

relation av_sin_catar retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarretinopatia_diabetica;
deterministic=false;
values= table (0.01 0.1 1.0 0.15 0.7 0.0 0.74 0.15 0.0 0.1 0.05 0.0 );
}

relation av_sin_catar retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarretinopatia_nd;
deterministic=false;
values= table (0.1 1.0 0.3 0.0 0.5 0.0 0.1 0.0 );
}

relation av_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_sin_catarResidual;
deterministic=false;
values= table (0.97 0.02 0.0099 1.0E-4 );
}

relation pupila_estrecha ojo_vitrectomizado { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = pupila_estrechaojo_vitrectomizado;
deterministic=false;
values= table (0.4 0.0 0.6 1.0 );
}

relation pupila_estrecha pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = pupila_estrechapseudoexfoliacion;
deterministic=false;
values= table (0.4 0.0 0.6 1.0 );
}

relation pupila_estrecha retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = pupila_estrecharetinopatia_diabetica;
deterministic=false;
values= table (0.65 0.6 0.0 0.35 0.4 1.0 );
}

relation pupila_estrecha sinequias_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = pupila_estrechasinequias_post;
deterministic=false;
values= table (0.8 0.0 0.2 1.0 );
}

relation pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = pupila_estrechaResidual;
deterministic=false;
values= table (0.015 0.985 );
}

relation fvnd_pre_catar av_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarav_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 );
}

relation fvnd_pre_catar tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catartipo_catarata;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 );
}

relation fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarResidual;
deterministic=false;
values= table (0.0 0.0 0.0 );
}

relation fvnd_contral av_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_contralav_contral;
deterministic=false;
values= table (0.0050 0.1 0.3 0.95 0.05 0.2 0.4 0.05 0.945 0.7 0.3 0.0 );
}

relation fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_contralResidual;
deterministic=false;
values= table (0.01 0.01 0.98 );
}

relation despr_coroideo camara_estrecha incision_anormal mala_colaboracion ruptura_caps_post { 
comment = "";
deterministic=false;
values= function  
          Or(despr_coroideocamara_estrecha,despr_coroideoincision_anormal,despr_coroideomala_colaboracion,despr_coroideoruptura_caps_post,despr_coroideoResidual);

henrionVSdiez = "Diez";
}

relation despr_coroideo camara_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_coroideocamara_estrecha;
deterministic=false;
values= table (0.0015 0.0 0.9985 1.0 );
}

relation despr_coroideo incision_anormal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_coroideoincision_anormal;
deterministic=false;
values= table (0.0020 0.0 0.998 1.0 );
}

relation despr_coroideo mala_colaboracion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_coroideomala_colaboracion;
deterministic=false;
values= table (0.0025 0.0 0.9975 1.0 );
}

relation despr_coroideo ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_coroideoruptura_caps_post;
deterministic=false;
values= table (0.0025 0.0 0.9975 1.0 );
}

relation despr_coroideo { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_coroideoResidual;
deterministic=false;
values= table (5.0E-5 0.99995 );
}

relation despr_retina mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_retinamecha_vitrea;
deterministic=false;
values= table (0.07 0.0 0.93 1.0 );
}

relation despr_retina miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_retinamiopia_magna;
deterministic=false;
values= table (0.07 0.0 0.93 1.0 );
}

relation despr_retina retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_retinaretinopatia_nd;
deterministic=false;
values= table (0.02 0.0 0.98 1.0 );
}

relation despr_retina { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = despr_retinaResidual;
deterministic=false;
values= table (5.0E-4 0.9995 );
}

relation endoftalmitis incision_anormal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = endoftalmitisincision_anormal;
deterministic=false;
values= table (0.01 0.0 0.99 1.0 );
}

relation endoftalmitis mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = endoftalmitismecha_vitrea;
deterministic=false;
values= table (0.0010 0.0 0.999 1.0 );
}

relation endoftalmitis ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = endoftalmitisruptura_caps_post;
deterministic=false;
values= table (0.0010 0.0 0.999 1.0 );
}

relation endoftalmitis { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = endoftalmitisResidual;
deterministic=false;
values= table (1.0E-5 0.99999 );
}

relation deslu_catar tipo_catarata { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.999 0.9 0.85 0.4 0.05 0.0010 0.1 0.15 0.6 0.95 );
}

relation deslu_post deslu_complic deslu_pre_no_catar { 
comment = "";
deterministic=true;
values= function  
          Or(deslu_postdeslu_complic,deslu_postdeslu_pre_no_catar,deslu_postResidual);

henrionVSdiez = "Diez";
}

relation deslu_post deslu_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_postdeslu_complic;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_post deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_postdeslu_pre_no_catar;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_postResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation fvnd_global_pre fvnd_contral fvnd_pre { 
comment = "";
deterministic=false;
values= function  
          Min(fvnd_global_prefvnd_contral,fvnd_global_prefvnd_pre,fvnd_global_preResidual);

}

relation fvnd_global_pre fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_prefvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_prefvnd_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_global_post fvnd_contral fvnd_post { 
comment = "";
deterministic=false;
values= function  
          Min(fvnd_global_postfvnd_contral,fvnd_global_postfvnd_post,fvnd_global_postResidual);

}

relation fvnd_global_post fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postfvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postfvnd_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_global_post fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postfvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postfvnd_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_postResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_pre otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_preotros_trast_fv;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_pre fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_prefvnd_pre_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_preResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_prefvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_prefvnd_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_global_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_pre_catar av_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarav_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 1.0 );
}

relation fvnd_pre_catar tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catartipo_catarata;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 );
}

relation fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation fvnd_pre_catar av_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarav_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 1.0 );
}

relation fvnd_pre_catar tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catartipo_catarata;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 );
}

relation fvnd_pre_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_catarResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation otros_trast_fv distrofia_fuchs maculopatias opac_corneales retinopatia_diabetica retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          CausalMax(otros_trast_fvdistrofia_fuchs,otros_trast_fvmaculopatias,otros_trast_fvopac_corneales,otros_trast_fvretinopatia_diabetica,otros_trast_fvretinopatia_nd,otros_trast_fvResidual);

henrionVSdiez = "Diez";
}

relation otros_trast_fv distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvdistrofia_fuchs;
deterministic=false;
values= table (0.15 0.0 0.3 0.0 0.55 1.0 );
}

relation otros_trast_fv maculopatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvmaculopatias;
deterministic=false;
values= table (0.05 0.0 0.15 0.0 0.8 1.0 );
}

relation otros_trast_fv opac_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvopac_corneales;
deterministic=false;
values= table (0.05 0.0 0.1 0.0 0.85 1.0 );
}

relation otros_trast_fv retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvretinopatia_diabetica;
deterministic=false;
values= table (0.25 0.1 0.0 0.65 0.26 0.0 0.1 0.64 1.0 );
}

relation otros_trast_fv retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvretinopatia_nd;
deterministic=false;
values= table (0.0 0.0 0.15 0.0 0.85 1.0 );
}

relation otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvResidual;
deterministic=false;
values= table (1.0E-4 0.0010 0.9989 );
}

relation deslu_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = DResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation deslu_complic edema_corneal edema_mac_cist { 
comment = "";
deterministic=false;
values= function  
          Or(deslu_complicedema_corneal,deslu_complicedema_mac_cist,deslu_complicResidual);

henrionVSdiez = "Diez";
}

relation deslu_complic edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_complicedema_corneal;
deterministic=false;
values= table (0.999 0.0 0.0010 1.0 );
}

relation deslu_complic edema_mac_cist { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_complicedema_mac_cist;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation deslu_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_complicResidual;
deterministic=false;
values= table (1.0E-4 0.9999 );
}

relation fvnd_post av_post otros_trast_fv otros_trast_fvnd_complic { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(fvnd_postav_post,fvnd_postotros_trast_fv,fvnd_postotros_trast_fvnd_complic,fvnd_postResidual);

}

relation fvnd_post av_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postav_post;
deterministic=false;
values= table (0.0050 0.1 0.3 0.95 0.05 0.2 0.4 0.05 0.945 0.7 0.3 0.0 );
}

relation fvnd_post otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postotros_trast_fv;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_post otros_trast_fvnd_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postotros_trast_fvnd_complic;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation fvnd_post av_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postav_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 1.0 );
}

relation fvnd_post otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postotros_trast_fv;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_post otros_trast_fvnd_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postotros_trast_fvnd_complic;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_postResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation deslu_pre_no_catar distrofia_fuchs maculopatias miopia_magna opac_corneales { 
comment = "";
deterministic=false;
values= function  
          Or(deslu_pre_no_catardistrofia_fuchs,deslu_pre_no_catarmaculopatias,deslu_pre_no_catarmiopia_magna,deslu_pre_no_cataropac_corneales,deslu_pre_no_catarResidual);

henrionVSdiez = "Diez";
}

relation deslu_pre_no_catar distrofia_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_catardistrofia_fuchs;
deterministic=false;
values= table (0.99 0.0 0.01 1.0 );
}

relation deslu_pre_no_catar maculopatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_catarmaculopatias;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation deslu_pre_no_catar miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_catarmiopia_magna;
deterministic=false;
values= table (0.5 0.0 0.5 1.0 );
}

relation deslu_pre_no_catar opac_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_cataropac_corneales;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_catarResidual;
deterministic=false;
values= table (5.0E-4 0.9995 );
}

relation av_complic despr_coroideo despr_retina edema_corneal edema_mac_cist endoftalmitis { 
comment = "";
deterministic=false;
values= function  
          Min(av_complicdespr_coroideo,av_complicdespr_retina,av_complicedema_corneal,av_complicedema_mac_cist,av_complicendoftalmitis,av_complicResidual);

}

relation av_complic despr_coroideo { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicdespr_coroideo;
deterministic=false;
values= table (0.2 1.0 0.4 0.0 0.3 0.0 0.1 0.0 );
}

relation av_complic despr_retina { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicdespr_retina;
deterministic=false;
values= table (0.01 1.0 0.15 0.0 0.65 0.0 0.19 0.0 );
}

relation av_complic edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicedema_corneal;
deterministic=false;
values= table (0.05 1.0 0.35 0.0 0.4 0.0 0.2 0.0 );
}

relation av_complic edema_mac_cist { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicedema_mac_cist;
deterministic=false;
values= table (0.0 1.0 0.05 0.0 0.7 0.0 0.25 0.0 );
}

relation av_complic endoftalmitis { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicendoftalmitis;
deterministic=false;
values= table (0.0 1.0 0.0 0.0 0.01 0.0 0.99 0.0 );
}

relation av_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicResidual;
deterministic=false;
values= table (0.99999 1.0E-5 0.0 0.0 );
}

relation fv_global_pre fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_prefvnd_global_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_post deslu_contral deslu_post fvnd_global_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(fv_global_postdeslu_contral,fv_global_postdeslu_post,fv_global_postfvnd_global_post,fv_global_postResidual);

henrionVSdiez = "Diez";
}

relation fv_global_post deslu_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_postdeslu_contral;
deterministic=false;
values= table (0.05 0.0 0.9 0.0 0.05 1.0 );
}

relation fv_global_post deslu_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_postdeslu_post;
deterministic=false;
values= table (0.05 0.0 0.9 0.0 0.05 1.0 );
}

relation fv_global_post fvnd_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_postfvnd_global_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_postResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation catarata_contral { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.01 0.0040 0.0040 0.3 0.182 0.5 );
}

relation av_contral catarata_contral { 
comment = "";
deterministic=false;
values= function  
          Min(av_contralcatarata_contral,av_contralResidual);

}

relation deslu_contral catarata_contral { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.999 0.9 0.85 0.4 0.05 0.0010 0.0010 0.1 0.15 0.6 0.95 0.999 );
}

relation deslu_global_pre deslu_pre deslu_contral { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.05 0.8 0.0010 0.0 0.05 0.0010 0.8 0.0 0.7 0.05 0.05 0.0 0.2 0.149 0.149 0.0 0.0 0.0 0.0 1.0 );
}

relation deslu_global_post deslu_post deslu_contral { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.05 0.8 0.0010 0.0 0.05 0.0010 0.8 0.0 0.7 0.05 0.05 0.0 0.2 0.149 0.149 0.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_pre deslu_contral deslu_pre fvnd_global_pre { 
comment = "";
deterministic=false;
values= function  
          CausalMax(fv_global_predeslu_contral,fv_global_predeslu_pre,fv_global_prefvnd_global_pre,fv_global_preResidual);

henrionVSdiez = "Diez";
}

relation fv_global_pre deslu_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_predeslu_contral;
deterministic=false;
values= table (0.05 0.0 0.9 0.0 0.05 1.0 );
}

relation fv_global_pre deslu_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_predeslu_pre;
deterministic=false;
values= table (0.05 0.0 0.9 0.0 0.05 1.0 );
}

relation fv_global_pre fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_prefvnd_global_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_preResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation fv_global_pre deslu_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_predeslu_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_pre deslu_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_predeslu_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_pre fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_prefvnd_global_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fv_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fv_global_preResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation contrala_RAND av_contral { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation agudepre_RAND av_pre { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation patolo_RAND retinopatia_diabetica distrofia_fuchs ambliopia maculopatias neuropatias opac_corneales retinopatia_nd { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation laterali_RAND catarata_contral { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation comtec_baja_RAND camara_estrecha miopia_magna ojo_hundido pupila_estrecha sinequias_post { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 );
}

relation comtec_med_RAND tipo_catarata mala_colaboracion ojo_vitrectomizado pseudoexfoliacion comtec_baja_RAND { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation av_contral catarata_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_contralcatarata_contral;
deterministic=false;
values= table (0.1 0.01 0.01 0.2 0.999 1.0 0.25 0.15 0.01 0.6 0.0010 0.0 0.4 0.3 0.3 0.19 0.0 0.0 0.25 0.54 0.68 0.01 0.0 0.0 );
}

relation av_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_contralResidual;
deterministic=false;
values= table (0.8 0.1 0.07 0.03 );
}

relation edema_mac_cist endoftalmitis mecha_vitrea pseudoexfoliacion retinopatia_diabetica retinopatia_nd ruptura_caps_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(edema_mac_cistendoftalmitis,edema_mac_cistmecha_vitrea,edema_mac_cistpseudoexfoliacion,edema_mac_cistretinopatia_diabetica,edema_mac_cistretinopatia_nd,edema_mac_cistruptura_caps_post,edema_mac_cistResidual);

henrionVSdiez = "Diez";
}

relation edema_mac_cist endoftalmitis { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistendoftalmitis;
deterministic=false;
values= table (0.95 0.0 0.05 1.0 );
}

relation edema_mac_cist mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistmecha_vitrea;
deterministic=false;
values= table (0.05 0.0 0.95 1.0 );
}

relation edema_mac_cist pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistpseudoexfoliacion;
deterministic=false;
values= table (0.04 0.0 0.96 1.0 );
}

relation edema_mac_cist retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistretinopatia_diabetica;
deterministic=false;
values= table (0.35 0.1 0.0 0.65 0.9 1.0 );
}

relation edema_mac_cist retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistretinopatia_nd;
deterministic=false;
values= table (0.01 0.0 0.99 1.0 );
}

relation edema_mac_cist ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistruptura_caps_post;
deterministic=false;
values= table (0.02 0.0 0.98 1.0 );
}

relation edema_mac_cist { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_mac_cistResidual;
deterministic=false;
values= table (1.0E-4 0.9999 );
}

relation ganancia_av av_pre av_post { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 );
}

relation ganancia_deslu deslu_pre deslu_post { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (0.0 1.0 0.0 0.0 1.0 0.0 0.0 1.0 0.0 0.0 1.0 0.0 );
}

relation deslu_pre deslu_catar deslu_pre_no_catar { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(deslu_predeslu_catar,deslu_predeslu_pre_no_catar,deslu_preResidual);

}

relation deslu_pre deslu_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_predeslu_catar;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_pre deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_predeslu_pre_no_catar;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_preResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation deslu_pre deslu_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_predeslu_catar;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_pre deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_predeslu_pre_no_catar;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_preResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation comtec_alta_RAND comtec_med_RAND fibrosis_c_ant sublux_cristalino tipo_catarata { 
comment = "";
deterministic=true;
values= function  
          CausalMax(comtec_alta_RANDcomtec_med_RAND,comtec_alta_RANDfibrosis_c_ant,comtec_alta_RANDsublux_cristalino,comtec_alta_RANDtipo_catarata,comtec_alta_RANDResidual);

henrionVSdiez = "Diez";
}

relation comtec_alta_RAND comtec_med_RAND { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDcomtec_med_RAND;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 1.0 );
}

relation comtec_alta_RAND fibrosis_c_ant { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDfibrosis_c_ant;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation comtec_alta_RAND sublux_cristalino { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDsublux_cristalino;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation comtec_alta_RAND tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDtipo_catarata;
deterministic=false;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 );
}

relation comtec_alta_RAND { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation otros_trast_fvnd_complic despr_coroideo despr_retina edema_corneal edema_mac_cist { 
comment = "";
deterministic=false;
values= function  
          CausalMax(otros_trast_fvnd_complicdespr_coroideo,otros_trast_fvnd_complicdespr_retina,otros_trast_fvnd_complicedema_corneal,otros_trast_fvnd_complicedema_mac_cist,otros_trast_fvnd_complicResidual);

henrionVSdiez = "Diez";
}

relation otros_trast_fvnd_complic despr_coroideo { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicdespr_coroideo;
deterministic=false;
values= table (0.0010 0.0 0.01 0.0 0.989 1.0 );
}

relation otros_trast_fvnd_complic despr_retina { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicdespr_retina;
deterministic=false;
values= table (0.0010 0.0 0.01 0.0 0.989 1.0 );
}

relation otros_trast_fvnd_complic edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicedema_corneal;
deterministic=false;
values= table (0.0 0.0 0.01 0.0 0.99 1.0 );
}

relation otros_trast_fvnd_complic edema_mac_cist { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicedema_mac_cist;
deterministic=false;
values= table (0.0 0.0 0.01 0.0 0.99 1.0 );
}

relation otros_trast_fvnd_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicResidual;
deterministic=false;
values= table (0.0 0.0 1.0 );
}

relation funcion_RAND deslu_global_pre fv_global_pre { 
comment = "";
deterministic=true;
values= function  
          CausalMax(funcion_RANDdeslu_global_pre,funcion_RANDfv_global_pre,funcion_RANDResidual);

henrionVSdiez = "Diez";
}

relation funcion_RAND deslu_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_RANDdeslu_global_pre;
deterministic=false;
values= table (0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation funcion_RAND fv_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_RANDfv_global_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation funcion_RAND { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_RANDResidual;
deterministic=false;
values= table (0.0 0.0 0.0 1.0 );
}

relation funcion_post deslu_global_post fv_global_post { 
comment = "";
deterministic=true;
values= function  
          CausalMax(funcion_postdeslu_global_post,funcion_postfv_global_post,funcion_postResidual);

henrionVSdiez = "Diez";
}

relation funcion_post deslu_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_postdeslu_global_post;
deterministic=false;
values= table (0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation funcion_post fv_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_postfv_global_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation funcion_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = funcion_postResidual;
deterministic=false;
values= table (0.0 0.0 0.0 1.0 );
}

relation agudepos_RAND av_sin_catar { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

}
