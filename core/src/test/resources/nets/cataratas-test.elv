// Bayesian Network
//   Elvira format 

bnet  "" { 

// Network Properties

kindofgraph = "mixed";
visualprecision = "0.0000";
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

// Links of the associated graph:

link ambliopia agudeza_vis_sin_catar;

link distrofia_corneal_fuchs agudeza_vis_sin_catar;

link distrofia_corneal_fuchs opacidades_corneales;

link maculopatias agudeza_vis_sin_catar;

link neuropatias agudeza_vis_sin_catar;

link opacidades_corneales agudeza_vis_sin_catar;

link retinopatia_diabetica agudeza_vis_sin_catar;

link retinopatia_diabetica maculopatias;

link retinopatia_nd agudeza_vis_sin_catar;

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

relation opacidades_corneales distrofia_corneal_fuchs { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.4 0.01 0.6 0.99 );
}

relation maculopatias retinopatia_diabetica { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.3 0.08 0.3 0.7 0.92 );
}

relation agudeza_vis_sin_catar ambliopia distrofia_corneal_fuchs maculopatias neuropatias opacidades_corneales retinopatia_diabetica retinopatia_nd { 
comment = "";
deterministic=false;
values= function  
          Min(agudeza_vis_sin_catarambliopia,agudeza_vis_sin_catardistrofia_corneal_fuchs,agudeza_vis_sin_catarmaculopatias,agudeza_vis_sin_catarneuropatias,agudeza_vis_sin_cataropacidades_corneales,agudeza_vis_sin_catarretinopatia_diabetica,agudeza_vis_sin_catarretinopatia_nd,agudeza_vis_sin_catarResidual);

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

relation D { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = DResidual;
deterministic=false;
values= table (0.0 1.0 );
}

}
