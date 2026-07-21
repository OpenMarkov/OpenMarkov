// Bayesian Network
//   Elvira format 

bnet  "" { 

// Network Properties

kindofgraph = "mixed";
visualprecision = "0.000000";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node agudeza_vis_sin_catar(finite-states) {
title = "av_sin_catar";
comment = "Dsiminución agudeza por causas distintas de la catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =960;
pos_y =261;
relevance = 9.0;
purpose = "The rank of values is: [0.0,1.0]";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node camara_estrecha(finite-states) {
title = "camara_estrecha";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =239;
pos_y =345;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node ojo_hundido(finite-states) {
title = "ojo_hundido";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =76;
pos_y =327;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node miopia_magna(finite-states) {
title = "miopia_magna";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =736;
pos_y =291;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node pupila_estrecha(finite-states) {
title = "pupila_estrecha";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =398;
pos_y =306;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node pseudoexfoliacion(finite-states) {
title = "pseudoexfoliacion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =536;
pos_y =325;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node tipo_catarata(finite-states) {
title = "tipo_catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =502;
pos_y =158;
relevance = 10.0;
purpose = "";
num-states = 5;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve");
}

node ojo_vitrectomizado(finite-states) {
title = "ojo_vitrectomizado";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =295;
pos_y =246;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node mala_colaboracion(finite-states) {
title = "mala_colaboracion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =88;
pos_y =144;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node retinopatia_diabetica(finite-states) {
title = "retinopatia_diabetica";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =126;
pos_y =55;
relevance = 6.0;
purpose = "";
num-states = 3;
states = ("proliferativa" "no proliferativa" "ausente");
}

node retinopatia_nd(finite-states) {
title = "retinopatia_nd";
comment = "No diabetica";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =259;
pos_y =130;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node maculopatias(finite-states) {
title = "maculopatias";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =365;
pos_y =63;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node neuropatias(finite-states) {
title = "neuropatias";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =757;
pos_y =86;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node ambliopia(finite-states) {
title = "ambliopia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =573;
pos_y =62;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node opacidades_corneales(finite-states) {
title = "opacidades_corneales";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1101;
pos_y =155;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node distrofia_corneal_fuchs(finite-states) {
title = "distrofia_corneal_fuchs";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =936;
pos_y =63;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node av_complic(finite-states) {
title = "av_complic";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =233;
pos_y =735;
relevance = 9.0;
purpose = "";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node incision_anormal(finite-states) {
title = "incision_anormal";
comment = "efectos a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =115;
pos_y =488;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node endoftalmitis(finite-states) {
title = "endoftalmitis";
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =316;
pos_y =567;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node edema_corneal(finite-states) {
title = "edema_corneal";
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =467;
pos_y =566;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node edema_macular_cistoide(finite-states) {
title = "edema_macular_cistoide";
comment = "clínico, a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =310;
pos_y =647;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node mecha_vitrea(finite-states) {
title = "mecha_vitrea";
comment = "perioperatoria";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =522;
pos_y =498;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node ruptura_caps_post(finite-states) {
title = "ruptura_caps_post";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =428;
pos_y =422;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node agudeza_visual_pre(finite-states) {
title = "av_pre";
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =929;
pos_y =387;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node av_post(finite-states) {
title = "av_post";
comment = "Agudeza visual  post-intervención,  corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =431;
pos_y =739;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node fvnd_pre_catar(finite-states) {
title = "fv-pre-catar";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =885;
pos_y =508;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node otros_trast_fv(finite-states) {
title = "otros-trast-fv";
comment = "Otros trastornos (distintos pérdida agudeza y deslu) no debidos a cataratas: brillo, contraste, campo, color, 3D...";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1021;
pos_y =488;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_pre(finite-states) {
title = "fvnd_pre";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =949;
pos_y =563;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node otros_trast_fvnd_complic(finite-states) {
title = "otros_trast_fvnd_complic";
comment = "Otros trastornos FV (distintos agudeza y deslu) debidos a complicaciones operacion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =678;
pos_y =659;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-problem");
}

node fvnd_post(finite-states) {
title = "fvnd_post";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =862;
pos_y =689;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-problem");
}

node av_contral(finite-states) {
title = "av_contral";
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1125;
pos_y =398;
relevance = 5.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node fvnd_contral(finite-states) {
title = "fvnd_contral";
comment = "Otros trastornos (distintos pérdida agudeza) no debidos a cataratas: brillo, contraste, campo, color, 3D...";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1118;
pos_y =459;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1094;
pos_y =607;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node fvnd_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1015;
pos_y =679;
relevance = 9.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

node despr_retina(finite-states) {
title = "despr_retina";
comment = "va a ser operado";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =620;
pos_y =564;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node Fibrosis_C_Ant(finite-states) {
title = "fibrosis_C_Ant";
comment = "Fibrosis de capsula 
anterior";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =588;
pos_y =266;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node sinequias_post(finite-states) {
title = "sinequias post";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =117;
pos_y =248;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node sublux_cristalino(finite-states) {
title = "sublux_cristalino";
comment = "subluxación del cristalino";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =661;
pos_y =382;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node despr_coroideo(finite-states) {
title = "desprend coroideo";
comment = "a largo plazo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =133;
pos_y =577;
relevance = 8.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_complic(finite-states) {
title = "deslu_complic";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =499;
pos_y =673;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_pre_no_catar(finite-states) {
title = "deslu_pre_no_catar";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =772;
pos_y =456;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_contral(finite-states) {
title = "deslu_contral";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1165;
pos_y =549;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "deslu_catar";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =666;
pos_y =497;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_pre(finite-states) {
title = "deslu_pre";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =822;
pos_y =577;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node deslu_post(finite-states) {
title = "deslu_post";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =628;
pos_y =751;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node fv_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =847;
pos_y =774;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "limit-ocio" "sin-limitaciones");
}

node fv_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1166;
pos_y =655;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "limit-ocio" "sin-limit");
}

node catarata_contral(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1160;
pos_y =266;
relevance = 7.0;
purpose = "";
num-states = 6;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve" "ausente");
}

node deslu_global_pre(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1002;
pos_y =733;
relevance = 7.0;
purpose = "";
num-states = 5;
states = ("ojo operar" "ojo contral" "ambos" "no sabe" "ausente");
}

node deslu_global_post(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1062;
pos_y =781;
relevance = 7.0;
purpose = "";
num-states = 5;
states = ("ojo operar" "ojo contral" "ambos" "no sabe" "ausente");
}

node contrala_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1201;
pos_y =429;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node agudepre_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =970;
pos_y =430;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node agudepos_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =441;
pos_y =792;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (">=0,5" "0,2_0,4" "=<0,1");
}

node patolo_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1150;
pos_y =59;
relevance = 7.0;
purpose = "";
num-states = 5;
states = ("asoc distrof corn" "imposible" "asoc otra patol" "asoc retinop diab" "catarata simple");
}

node laterali_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1199;
pos_y =333;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("bilateral" "unilateral");
}

node comtec_lev_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =86;
pos_y =398;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (">2 leves" "0-1 leves");
}

node comtec_mod_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =109;
pos_y =533;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (">2 mod" "1 mod" "ninguna mod");
}

node comtec_alta_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =100;
pos_y =663;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("alta" "nula/baja/mod");
}

node funcion_RAND(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1162;
pos_y =721;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("limit-diaria" "limit-ocio" "deslu" "sin-limit");
}

// Links of the associated graph:

link D deslu_pre;

link Fibrosis_C_Ant comtec_alta_RAND;

link Fibrosis_C_Ant ruptura_caps_post;

link agudeza_vis_sin_catar agudeza_visual_pre;

link agudeza_vis_sin_catar av_post;

link agudeza_visual_pre agudepre_RAND;

link agudeza_visual_pre fvnd_pre_catar;

link ambliopia agudeza_vis_sin_catar;

link ambliopia patolo_RAND;

link av_complic av_post;

link av_contral contrala_RAND;

link av_contral fvnd_contral;

link av_post agudepos_RAND;

link av_post fvnd_post;

link camara_estrecha comtec_lev_RAND;

link camara_estrecha despr_coroideo;

link camara_estrecha edema_corneal;

link camara_estrecha incision_anormal;

link camara_estrecha ruptura_caps_post;

link catarata_contral av_contral;

link catarata_contral deslu_contral;

link catarata_contral laterali_RAND;

link comtec_lev_RAND comtec_mod_RAND;

link comtec_mod_RAND comtec_alta_RAND;

link deslu_complic deslu_post;

link deslu_contral deslu_global_post;

link deslu_contral deslu_global_pre;

link deslu_contral fv_global_post;

link deslu_contral fv_global_pre;

link deslu_global_pre funcion_RAND;

link deslu_post deslu_global_post;

link deslu_post fv_global_post;

link deslu_pre deslu_global_pre;

link deslu_pre fv_global_pre;

link deslu_pre_no_catar deslu_post;

link deslu_pre_no_catar deslu_pre;

link despr_coroideo av_complic;

link despr_coroideo otros_trast_fvnd_complic;

link despr_retina av_complic;

link despr_retina otros_trast_fvnd_complic;

link distrofia_corneal_fuchs agudeza_vis_sin_catar;

link distrofia_corneal_fuchs deslu_pre_no_catar;

link distrofia_corneal_fuchs edema_corneal;

link distrofia_corneal_fuchs opacidades_corneales;

link distrofia_corneal_fuchs otros_trast_fv;

link distrofia_corneal_fuchs patolo_RAND;

link edema_corneal av_complic;

link edema_corneal deslu_complic;

link edema_corneal otros_trast_fvnd_complic;

link edema_macular_cistoide av_complic;

link edema_macular_cistoide deslu_complic;

link edema_macular_cistoide otros_trast_fvnd_complic;

link endoftalmitis av_complic;

link endoftalmitis edema_corneal;

link endoftalmitis edema_macular_cistoide;

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

link maculopatias agudeza_vis_sin_catar;

link maculopatias deslu_pre_no_catar;

link maculopatias otros_trast_fv;

link maculopatias patolo_RAND;

link mala_colaboracion comtec_mod_RAND;

link mala_colaboracion despr_coroideo;

link mala_colaboracion incision_anormal;

link mala_colaboracion ruptura_caps_post;

link mecha_vitrea despr_retina;

link mecha_vitrea endoftalmitis;

link miopia_magna comtec_lev_RAND;

link miopia_magna deslu_pre_no_catar;

link miopia_magna despr_retina;

link miopia_magna incision_anormal;

link miopia_magna mecha_vitrea;

link neuropatias agudeza_vis_sin_catar;

link neuropatias patolo_RAND;

link ojo_hundido comtec_lev_RAND;

link ojo_hundido edema_corneal;

link ojo_hundido incision_anormal;

link ojo_hundido ruptura_caps_post;

link ojo_vitrectomizado comtec_mod_RAND;

link ojo_vitrectomizado pupila_estrecha;

link ojo_vitrectomizado ruptura_caps_post;

link opacidades_corneales agudeza_vis_sin_catar;

link opacidades_corneales deslu_pre_no_catar;

link opacidades_corneales otros_trast_fv;

link opacidades_corneales patolo_RAND;

link otros_trast_fv fvnd_post;

link otros_trast_fv fvnd_pre;

link otros_trast_fvnd_complic fvnd_post;

link pseudoexfoliacion comtec_mod_RAND;

link pseudoexfoliacion edema_macular_cistoide;

link pseudoexfoliacion incision_anormal;

link pseudoexfoliacion mecha_vitrea;

link pseudoexfoliacion pupila_estrecha;

link pseudoexfoliacion ruptura_caps_post;

link pupila_estrecha comtec_lev_RAND;

link pupila_estrecha incision_anormal;

link pupila_estrecha mecha_vitrea;

link pupila_estrecha ruptura_caps_post;

link retinopatia_diabetica agudeza_vis_sin_catar;

link retinopatia_diabetica edema_macular_cistoide;

link retinopatia_diabetica maculopatias;

link retinopatia_diabetica otros_trast_fv;

link retinopatia_diabetica patolo_RAND;

link retinopatia_diabetica pupila_estrecha;

link retinopatia_nd agudeza_vis_sin_catar;

link retinopatia_nd despr_retina;

link retinopatia_nd edema_macular_cistoide;

link retinopatia_nd otros_trast_fv;

link retinopatia_nd patolo_RAND;

link ruptura_caps_post despr_coroideo;

link ruptura_caps_post edema_macular_cistoide;

link ruptura_caps_post endoftalmitis;

link ruptura_caps_post mecha_vitrea;

link sinequias_post comtec_lev_RAND;

link sinequias_post pupila_estrecha;

link sublux_cristalino comtec_alta_RAND;

link sublux_cristalino mecha_vitrea;

link tipo_catarata D;

link tipo_catarata agudeza_visual_pre;

link tipo_catarata comtec_alta_RAND;

link tipo_catarata comtec_mod_RAND;

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

relation distrofia_corneal_fuchs { 
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

relation opacidades_corneales distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.4 0.01 0.6 0.99 );
}

relation agudeza_visual_pre agudeza_vis_sin_catar tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          Min(agudeza_visual_preagudeza_vis_sin_catar,agudeza_visual_pretipo_catarata,agudeza_visual_preResidual);

}

relation maculopatias retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.3 0.08 0.3 0.7 0.92 );
}

relation agudeza_visual_pre agudeza_vis_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_visual_preagudeza_vis_sin_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation agudeza_visual_pre tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_visual_pretipo_catarata;
deterministic=false;
values= table (0.1 0.01 0.01 0.2 0.999 0.25 0.15 0.01 0.6 0.0010 0.4 0.3 0.3 0.19 0.0 0.25 0.54 0.68 0.01 0.0 );
}

relation agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_visual_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 );
}

relation tipo_catarata { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.03 0.03 0.8 0.12 );
}

relation edema_corneal camara_estrecha distrofia_corneal_fuchs endoftalmitis ojo_hundido tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          CausalMax(edema_cornealcamara_estrecha,edema_cornealdistrofia_corneal_fuchs,edema_cornealendoftalmitis,edema_cornealojo_hundido,edema_cornealtipo_catarata,edema_cornealResidual);

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

relation edema_corneal distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealdistrofia_corneal_fuchs;
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
values= table (1.0E-4 0.01 0.01 1.0E-4 0.0 0.9999 0.99 0.99 0.9999 1.0 );
}

relation edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_cornealResidual;
deterministic=false;
values= table (1.0E-5 0.99999 );
}

relation av_post agudeza_vis_sin_catar av_complic { 
comment = "";
deterministic=true;
values= function  
          Min(av_postagudeza_vis_sin_catar,av_postagudeza_visual_complic,av_postResidual);

}

relation av_post agudeza_vis_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_postagudeza_vis_sin_catar;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation av_post av_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_postagudeza_visual_complic;
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
          CausalMax(fvnd_prefvnd_no_catar,fvnd_prefvnd_pre_catar,fvnd_preResidual);

henrionVSdiez = "Diez";
}

relation agudeza_vis_sin_catar ambliopia distrofia_corneal_fuchs maculopatias neuropatias opacidades_corneales retinopatia_diabetica retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          Min(agudeza_vis_sin_catarambliopia,agudeza_vis_sin_catardistrofia_corneal_fuchs,agudeza_vis_sin_catarmaculopatias,agudeza_vis_sin_catarneuropatias,agudeza_vis_sin_cataropacidades_corneales,agudeza_vis_sin_catarretinopatia_diabetica,agudeza_vis_sin_catarretinopatia_nd,agudeza_vis_sin_catarResidual);

}

relation fvnd_pre_catar agudeza_visual_pre tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(fvnd_pre_cataragudeza_visual_pre,fvnd_pre_catartipo_catarata,fvnd_pre_catarResidual);

}

relation fvnd_pre otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_prefvnd_no_catar;
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
          CausalMax(incision_anormalcamara_estrecha,incision_anormalmala_colaboracion_paciente,incision_anormalmiopia_magna,incision_anormalojo_hundido,incision_anormalpseudoexfoliacion,incision_anormalpupila_estrecha,incision_anormaltipo_catarata,incision_anormalResidual);

henrionVSdiez = "Diez";
}

relation edema_macular_cistoide endoftalmitis pseudoexfoliacion retinopatia_diabetica retinopatia_nd ruptura_caps_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(edema_macular_cistoideendoftalmitis,edema_macular_cistoidepseudoexfoliacion,edema_macular_cistoideretinopatia_diabetica,edema_macular_cistoideretinopatia_nd,edema_macular_cistoideruptura_caps_post,edema_macular_cistoideResidual);

henrionVSdiez = "Diez";
}

relation endoftalmitis incision_anormal mecha_vitrea ruptura_caps_post { 
comment = "";
deterministic=false;
values= function  
          CausalMax(endoftalmitisincision_anormal,endoftalmitismecha_vitrea,endoftalmitisruptura_caps_post,endoftalmitisResidual);

henrionVSdiez = "Diez";
}

relation Fibrosis_C_Ant { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0030 0.997 );
}

relation ruptura_caps_post Fibrosis_C_Ant camara_estrecha mala_colaboracion ojo_hundido ojo_vitrectomizado pseudoexfoliacion pupila_estrecha tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          CausalMax(ruptura_caps_postFibrosis_C_Ant,ruptura_caps_postcamara_estrecha,ruptura_caps_postmala_colaboracion_paciente,ruptura_caps_postojo_hundido,ruptura_caps_postojo_vitrectomizado,ruptura_caps_postpseudoexfoliacion,ruptura_caps_postpupila_estrecha,ruptura_caps_posttipo_catarata,ruptura_caps_postResidual);

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

relation edema_macular_cistoide endoftalmitis { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoideendoftalmitis;
deterministic=false;
values= table (0.95 0.0 0.05 1.0 );
}

relation edema_macular_cistoide pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoidepseudoexfoliacion;
deterministic=false;
values= table (0.04 0.0 0.96 1.0 );
}

relation edema_macular_cistoide retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoideretinopatia_diabetica;
deterministic=false;
values= table (0.35 0.1 0.0 0.65 0.9 1.0 );
}

relation edema_macular_cistoide retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoideretinopatia_nd;
deterministic=false;
values= table (0.01 0.0 0.99 1.0 );
}

relation edema_macular_cistoide ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoideruptura_caps_post;
deterministic=false;
values= table (0.02 0.0 0.98 1.0 );
}

relation edema_macular_cistoide { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = edema_macular_cistoideResidual;
deterministic=false;
values= table (1.0E-4 0.9999 );
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
values= table (0.1 0.0 0.9 1.0 );
}

relation mecha_vitrea pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreapupila_estrecha;
deterministic=false;
values= table (0.03 0.0 0.97 1.0 );
}

relation mecha_vitrea ruptura_caps_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitrearuptura_caps_post;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation mecha_vitrea sublux_cristalino { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreasublux_cristalino;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation mecha_vitrea { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = mecha_vitreaResidual;
deterministic=false;
values= table (0.02 0.98 );
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
values= table (0.15 0.0 0.85 1.0 );
}

relation incision_anormal mala_colaboracion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalmala_colaboracion_paciente;
deterministic=false;
values= table (0.4 0.0 0.6 1.0 );
}

relation incision_anormal miopia_magna { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalmiopia_magna;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation incision_anormal ojo_hundido { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalojo_hundido;
deterministic=false;
values= table (0.4 0.0 0.6 1.0 );
}

relation incision_anormal pseudoexfoliacion { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalpseudoexfoliacion;
deterministic=false;
values= table (0.1 0.0 0.9 1.0 );
}

relation incision_anormal pupila_estrecha { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalpupila_estrecha;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation incision_anormal tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormaltipo_catarata;
deterministic=false;
values= table (0.0 0.15 0.1 0.01 0.0 1.0 0.85 0.9 0.99 1.0 );
}

relation incision_anormal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = incision_anormalResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation ruptura_caps_post Fibrosis_C_Ant { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = ruptura_caps_postFibrosis_C_Ant;
deterministic=false;
values= table (0.15 0.0 0.85 1.0 );
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
name-of-relation = ruptura_caps_postmala_colaboracion_paciente;
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

relation fvnd_pre_catar agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_cataragudeza_visual_pre;
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

relation agudeza_vis_sin_catar ambliopia { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarambliopia;
deterministic=false;
values= table (0.15 1.0 0.65 0.0 0.15 0.0 0.05 0.0 );
}

relation agudeza_vis_sin_catar distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catardistrofia_corneal_fuchs;
deterministic=false;
values= table (0.1 1.0 0.4 0.0 0.4 0.0 0.1 0.0 );
}

relation agudeza_vis_sin_catar maculopatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarmaculopatias;
deterministic=false;
values= table (0.01 1.0 0.04 0.0 0.8 0.0 0.15 0.0 );
}

relation agudeza_vis_sin_catar neuropatias { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarneuropatias;
deterministic=false;
values= table (0.05 1.0 0.1 0.0 0.5 0.0 0.35 0.0 );
}

relation agudeza_vis_sin_catar opacidades_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_cataropacidades_corneales;
deterministic=false;
values= table (0.3 1.0 0.6 0.0 0.05 0.0 0.05 0.0 );
}

relation agudeza_vis_sin_catar retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarretinopatia_diabetica;
deterministic=false;
values= table (0.01 0.1 1.0 0.15 0.7 0.0 0.74 0.15 0.0 0.1 0.05 0.0 );
}

relation agudeza_vis_sin_catar retinopatia_nd { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarretinopatia_nd;
deterministic=false;
values= table (0.1 1.0 0.3 0.0 0.5 0.0 0.1 0.0 );
}

relation agudeza_vis_sin_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = agudeza_vis_sin_catarResidual;
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

relation fvnd_pre_catar agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_cataragudeza_visual_pre;
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

relation otros_trast_fvnd_complic edema_corneal { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_complicedema_corneal;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 );
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
values= table (0.05 0.0 0.95 1.0 );
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

relation deslu_pre deslu_pre_no_catar D { 
comment = "";
deterministic=true;
values= function  
          Or(deslu_preB,deslu_preD,deslu_preResidual);

henrionVSdiez = "Diez";
}

relation D tipo_catarata { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.999 0.9 0.85 0.4 0.05 0.0010 0.1 0.15 0.6 0.95 );
}

relation deslu_post deslu_complic deslu_pre_no_catar { 
comment = "";
deterministic=true;
values= function  
          Or(deslu_postA,deslu_postB,deslu_postResidual);

henrionVSdiez = "Diez";
}

relation deslu_pre deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_preB;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_pre D { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_preD;
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

relation deslu_post deslu_complic { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_postA;
deterministic=false;
values= table (1.0 0.0 0.0 1.0 );
}

relation deslu_post deslu_pre_no_catar { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_postB;
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

relation otros_trast_fvnd_complic edema_macular_cistoide { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_complicedema_macular_cistoide;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_contral fvnd_pre { 
comment = "";
deterministic=false;
values= function  
          Min(fvd_global_prefvnd_contral,fvd_global_prefvnd_pre,fvd_global_preResidual);

}

relation fvnd_global_pre fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_prefvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_prefvnd_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_global_post fvnd_contral fvnd_post { 
comment = "";
deterministic=false;
values= function  
          Min(fvd_global_postfvnd_contral,fvd_global_postfvnd_post,fvd_global_postResidual);

}

relation fvnd_global_post fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postfvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postfvnd_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_global_post fvnd_contral { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postfvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post fvnd_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postfvnd_post;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_post { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_postResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_pre otros_trast_fv { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_prefvnd_no_catar;
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
name-of-relation = fvd_global_prefvnd_contral;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre fvnd_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_prefvnd_pre;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 );
}

relation fvnd_global_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvd_global_preResidual;
deterministic=false;
values= table (1.0 0.0 0.0 );
}

relation fvnd_pre_catar agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_cataragudeza_visual_pre;
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

relation fvnd_pre_catar agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = fvnd_pre_cataragudeza_visual_pre;
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

relation otros_trast_fv distrofia_corneal_fuchs maculopatias opacidades_corneales retinopatia_diabetica retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          CausalMax(otros_trast_fvdistrofia_corneal_fuchs,otros_trast_fvmaculopatias,otros_trast_fvopacidades_corneales,otros_trast_fvretinopatia_diabetica,otros_trast_fvretinopatia_nd,otros_trast_fvResidual);

henrionVSdiez = "Diez";
}

relation otros_trast_fv distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvdistrofia_corneal_fuchs;
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

relation otros_trast_fv opacidades_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvopacidades_corneales;
deterministic=false;
values= table (0.05 0.0 0.1 0.0 0.85 1.0 );
}

relation otros_trast_fv retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvretinopatia_diabetica;
deterministic=false;
values= table (0.1 0.0 0.0 0.25 0.0 0.0 0.65 1.0 1.0 );
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

relation D { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = DResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation otros_trast_fvnd_complic despr_coroideo despr_retina edema_corneal edema_macular_cistoide { 
comment = "";
deterministic=false;
values= function  
          CausalMax(otros_trast_fvnd_complicdespr_coroideo,otros_trast_fvnd_complicdespr_retina,otros_trast_fvnd_complicedema_corneal,otros_trast_fvnd_complicedema_macular_cistoide,otros_trast_fvnd_complicResidual);

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

relation otros_trast_fvnd_complic edema_macular_cistoide { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = otros_trast_fvnd_complicedema_macular_cistoide;
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

relation deslu_complic edema_corneal edema_macular_cistoide { 
comment = "";
deterministic=false;
values= function  
          Or(deslu_complicedema_corneal,deslu_complicedema_macular_cistoide,deslu_complicResidual);

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

relation deslu_complic edema_macular_cistoide { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_complicedema_macular_cistoide;
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

relation deslu_pre_no_catar distrofia_corneal_fuchs maculopatias miopia_magna opacidades_corneales { 
comment = "";
deterministic=false;
values= function  
          Or(deslu_pre_no_catardistrofia_corneal_fuchs,deslu_pre_no_catarmaculopatias,deslu_pre_no_catarmiopia_magna,deslu_pre_no_cataropacidades_corneales,deslu_pre_no_catarResidual);

henrionVSdiez = "Diez";
}

relation deslu_pre_no_catar distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_catardistrofia_corneal_fuchs;
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

relation deslu_pre_no_catar opacidades_corneales { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = deslu_pre_no_cataropacidades_corneales;
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

relation av_complic despr_coroideo despr_retina edema_corneal edema_macular_cistoide endoftalmitis { 
comment = "";
deterministic=false;
values= function  
          Min(av_complicdespr_coroideo,av_complicdespr_retina,av_complicedema_corneal,av_complicedema_macular_cistoide,av_complicendoftalmitis,av_complicResidual);

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

relation av_complic edema_macular_cistoide { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = av_complicedema_macular_cistoide;
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
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.01 0.01 0.2 0.999 1.0 0.25 0.15 0.01 0.6 0.0010 0.0 0.4 0.3 0.3 0.19 0.0 0.0 0.25 0.54 0.68 0.01 0.0 0.0 );
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

relation agudepre_RAND agudeza_visual_pre { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation agudepos_RAND av_post { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation patolo_RAND retinopatia_diabetica distrofia_corneal_fuchs ambliopia maculopatias neuropatias opacidades_corneales retinopatia_nd { 
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

relation comtec_lev_RAND camara_estrecha miopia_magna ojo_hundido pupila_estrecha sinequias_post { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 1.0 );
}

relation comtec_mod_RAND tipo_catarata mala_colaboracion ojo_vitrectomizado pseudoexfoliacion comtec_lev_RAND { 
comment = "";
kind-of-relation = potential;
deterministic=true;
values= table (1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 1.0 1.0 1.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 1.0 0.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 );
}

relation comtec_alta_RAND tipo_catarata Fibrosis_C_Ant sublux_cristalino comtec_mod_RAND { 
comment = "";
deterministic=false;
values= function  
          CausalMax(comtec_alta_RANDtipo_catarata,comtec_alta_RANDFibrosis_C_Ant,comtec_alta_RANDsublux_cristalino,comtec_alta_RANDcomtec_mod_RAND,comtec_alta_RANDResidual);

henrionVSdiez = "Diez";
}

relation comtec_alta_RAND tipo_catarata { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDtipo_catarata;
deterministic=false;
values= table (1.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 );
}

relation comtec_alta_RAND Fibrosis_C_Ant { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDFibrosis_C_Ant;
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

relation comtec_alta_RAND comtec_mod_RAND { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDcomtec_mod_RAND;
deterministic=false;
values= table (1.0 0.0 0.0 0.0 1.0 1.0 );
}

relation comtec_alta_RAND { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = comtec_alta_RANDResidual;
deterministic=false;
values= table (0.0 1.0 );
}

relation funcion_RAND fv_global_pre deslu_global_pre { 
comment = "";
deterministic=true;
values= function  
          CausalMax(funcion_RANDfv_global_pre,funcion_RANDdeslu_global_pre,funcion_RANDResidual);

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

}
