// Bayesian Network
//   Elvira format 

bnet  "" { 

// Network Properties

kindofgraph = "mixed";
visualprecision = "0.00000000";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node agudeza_vis_sin_catar(finite-states) {
title = "av_sin_catar";
comment = "Dsiminución agudeza por causas distintas de la catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1155;
pos_y =86;
relevance = 9.0;
purpose = "The rank of values is: [0.0,1.0]";
num-states = 4;
states = ("(0.7,1]" "(0.4,0.7]" "(0.15,0.4]" "[0,0.15]");
}

node tipo_catarata(finite-states) {
title = "tipo_catarata";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =747;
pos_y =97;
relevance = 10.0;
purpose = "";
num-states = 5;
states = ("polar posterior" "brunescente" "blanca" "moderada" "leve");
}

node agudeza_visual_pre(finite-states) {
title = "av_pre";
comment = "Corregida";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =940;
pos_y =260;
relevance = 10.0;
purpose = "";
num-states = 4;
states = ("(0.7, 1]" "(0.4, 0.7]" "(0.15, 0.4]" "[0, 0.15]");
}

// Links of the associated graph:

link agudeza_vis_sin_catar agudeza_visual_pre;

link tipo_catarata agudeza_visual_pre;

//Network Relationships: 

relation agudeza_visual_pre agudeza_vis_sin_catar tipo_catarata { 
comment = "";
deterministic=false;
values= function  
          Min(agudeza_visual_preagudeza_vis_sin_catar,agudeza_visual_pretipo_catarata,agudeza_visual_preResidual);

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

relation agudeza_vis_sin_catar { 
comment = "";
deterministic=false;
values= table (0.25 0.25 0.25 0.25 );
}

}
