//	   Network
//	   Elvira format

bnet "" {

//		 Network Properties

kindofgraph = "mixed";
default node states = ("presente" , "ausente");

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
pos_x =431;
pos_y =316;
relevance = 5.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node pseudoexfoliacion(finite-states) {
title = "pseudoexfoliacion";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =599;
pos_y =345;
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
pos_x =578;
pos_y =65;
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
comment = "";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =415;
pos_y =423;
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
title = "";
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
title = "";
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
comment = "";
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
pos_x =634;
pos_y =465;
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
title = "";
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
title = "";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1169;
pos_y =683;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "limit-ocio" "sin-limit");
}

node catarata_contral(finite-states) {
title = "";
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
title = "";
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
title = "";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1143;
pos_y =761;
relevance = 7.0;
purpose = "";
num-states = 5;
states = ("ojo operar" "ojo contral" "ambos" "no sabe" "ausente");
}

//		 Links of the associated graph:

link agudeza_vis_sin_catar agudeza_visual_pre;
link agudeza_vis_sin_catar av_post;
link camara_estrecha despr_coroideo;
link camara_estrecha edema_corneal;
link camara_estrecha incision_anormal;
link camara_estrecha ruptura_caps_post;
link ojo_hundido edema_corneal;
link ojo_hundido incision_anormal;
link ojo_hundido ruptura_caps_post;
link miopia_magna deslu_pre_no_catar;
link miopia_magna despr_retina;
link miopia_magna incision_anormal;
link miopia_magna mecha_vitrea;
link pupila_estrecha incision_anormal;
link pupila_estrecha mecha_vitrea;
link pupila_estrecha ruptura_caps_post;
link pseudoexfoliacion edema_macular_cistoide;
link pseudoexfoliacion incision_anormal;
link pseudoexfoliacion mecha_vitrea;
link pseudoexfoliacion pupila_estrecha;
link pseudoexfoliacion ruptura_caps_post;
link tipo_catarata D;
link tipo_catarata agudeza_visual_pre;
link tipo_catarata edema_corneal;
link tipo_catarata fvnd_pre_catar;
link tipo_catarata incision_anormal;
link tipo_catarata ruptura_caps_post;
link ojo_vitrectomizado pupila_estrecha;
link ojo_vitrectomizado ruptura_caps_post;
link mala_colaboracion despr_coroideo;
link mala_colaboracion incision_anormal;
link mala_colaboracion ruptura_caps_post;
link retinopatia_diabetica agudeza_vis_sin_catar;
link retinopatia_diabetica edema_macular_cistoide;
link retinopatia_diabetica maculopatias;
link retinopatia_diabetica otros_trast_fv;
link retinopatia_diabetica pupila_estrecha;
link retinopatia_nd agudeza_vis_sin_catar;
link retinopatia_nd despr_retina;
link retinopatia_nd edema_macular_cistoide;
link retinopatia_nd otros_trast_fv;
link maculopatias agudeza_vis_sin_catar;
link maculopatias deslu_pre_no_catar;
link maculopatias otros_trast_fv;
link neuropatias agudeza_vis_sin_catar;
link ambliopia agudeza_vis_sin_catar;
link opacidades_corneales agudeza_vis_sin_catar;
link opacidades_corneales deslu_pre_no_catar;
link opacidades_corneales otros_trast_fv;
link distrofia_corneal_fuchs agudeza_vis_sin_catar;
link distrofia_corneal_fuchs deslu_pre_no_catar;
link distrofia_corneal_fuchs edema_corneal;
link distrofia_corneal_fuchs opacidades_corneales;
link distrofia_corneal_fuchs otros_trast_fv;
link av_complic av_post;
link incision_anormal despr_coroideo;
link incision_anormal endoftalmitis;
link endoftalmitis av_complic;
link endoftalmitis edema_corneal;
link endoftalmitis edema_macular_cistoide;
link edema_corneal av_complic;
link edema_corneal deslu_complic;
link edema_corneal otros_trast_fvnd_complic;
link edema_macular_cistoide av_complic;
link edema_macular_cistoide deslu_complic;
link edema_macular_cistoide otros_trast_fvnd_complic;
link mecha_vitrea despr_retina;
link mecha_vitrea endoftalmitis;
link ruptura_caps_post despr_coroideo;
link ruptura_caps_post edema_macular_cistoide;
link ruptura_caps_post endoftalmitis;
link ruptura_caps_post mecha_vitrea;
link agudeza_visual_pre fvnd_pre_catar;
link av_post fvnd_post;
link fvnd_pre_catar fvnd_pre;
link otros_trast_fv fvnd_post;
link otros_trast_fv fvnd_pre;
link fvnd_pre fvnd_global_pre;
link otros_trast_fvnd_complic fvnd_post;
link fvnd_post fvnd_global_post;
link av_contral fvnd_contral;
link fvnd_contral fvnd_global_post;
link fvnd_contral fvnd_global_pre;
link fvnd_global_pre fv_global_pre;
link fvnd_global_post fv_global_post;
link despr_retina av_complic;
link despr_retina otros_trast_fvnd_complic;
link Fibrosis_C_Ant ruptura_caps_post;
link sinequias_post pupila_estrecha;
link sublux_cristalino mecha_vitrea;
link despr_coroideo av_complic;
link despr_coroideo otros_trast_fvnd_complic;
link deslu_complic deslu_post;
link deslu_pre_no_catar deslu_post;
link deslu_pre_no_catar deslu_pre;
link deslu_contral deslu_global_post;
link deslu_contral deslu_global_pre;
link deslu_contral fv_global_post;
link deslu_contral fv_global_pre;
link D deslu_pre;
link deslu_pre deslu_global_pre;
link deslu_pre fv_global_pre;
link deslu_post deslu_global_post;
link deslu_post fv_global_post;
link catarata_contral av_contral;
link catarata_contral deslu_contral;
//		Network Relationships:

relation agudeza_vis_sin_catar ambliopia distrofia_corneal_fuchs maculopatias neuropatias opacidades_corneales retinopatia_diabetica retinopatia_nd {
