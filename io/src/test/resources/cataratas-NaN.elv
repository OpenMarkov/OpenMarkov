// Bayesian Network
//   Elvira format 

bnet  "" { 

// Network Properties

kindofgraph = "mixed";
visualprecision = "0.00000";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node agudeza_vis_sin_catar(finite-states) {
title = "av_sin_catar";
comment = "Disminucion agudeza por causas distintas de la catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =466;
pos_y =310;
relevance = 9.0;
purpose = "The rank of values is: [0.0,1.0]";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node tipo_catarata(finite-states) {
title = "tipo_catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =229;
pos_y =377;
relevance = 10.0;
purpose = "";
num-states = 5;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve");
}

node retinopatia_diabetica(finite-states) {
title = "retinopatia_diabetica";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =122;
pos_y =98;
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
pos_x =698;
pos_y =182;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node maculopatias(finite-states) {
title = "maculopatias";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =142;
pos_y =201;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node neuropatias(finite-states) {
title = "neuropatias";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =340;
pos_y =44;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node ambliopia(finite-states) {
title = "ambliopia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =448;
pos_y =139;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node distrofia_corneal_fuchs(finite-states) {
title = "distrofia_corneal_fuchs";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =655;
pos_y =85;
relevance = 6.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node agudeza_visual_pre(finite-states) {
title = "av_pre";
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =530;
pos_y =472;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

node fvnd_pre_catar(finite-states) {
title = "fv-pre-catar";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =359;
pos_y =623;
relevance = 10.0;
purpose = "";
num-states = 3;
states = ("lim-diaria" "lim-ocio" "sin-probl");
}

// Links of the associated graph:

link agudeza_vis_sin_catar agudeza_visual_pre;

link agudeza_visual_pre fvnd_pre_catar;

link ambliopia agudeza_vis_sin_catar;

link distrofia_corneal_fuchs agudeza_vis_sin_catar;

link maculopatias agudeza_vis_sin_catar;

link neuropatias agudeza_vis_sin_catar;

link retinopatia_diabetica agudeza_vis_sin_catar;

link retinopatia_diabetica maculopatias;

link retinopatia_nd agudeza_vis_sin_catar;

link tipo_catarata agudeza_visual_pre;

link tipo_catarata fvnd_pre_catar;

//Network Relationships: 

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

relation fvnd_pre_catar agudeza_visual_pre tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(fvnd_pre_cataragudeza_visual_pre,fvnd_pre_catartipo_catarata,fvnd_pre_catarResidual);

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

relation agudeza_vis_sin_catar ambliopia distrofia_corneal_fuchs maculopatias neuropatias retinopatia_diabetica retinopatia_nd { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 0.25 );
}

}
