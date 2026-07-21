// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node Diagnostico(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =410;
pos_y =106;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("Edwards" "Down" "Normal");
}

node Test_fetal(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =144;
pos_y =296;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("positivo" "negativo");
}

node Scr_2_tr(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =358;
pos_y =361;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("positivo" "negativo");
}

node Amniocentesis(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =597;
pos_y =322;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("positivo" "negativo");
}

node ECO_2_tr(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =769;
pos_y =239;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("positivo" "negativo");
}

// Links of the associated graph:

link Diagnostico Amniocentesis;

link Diagnostico ECO_2_tr;

link Diagnostico Scr_2_tr;

link Diagnostico Test_fetal;

//Network Relationships: 

relation Diagnostico { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (7.0E-4 0.0018 0.9975 );
}

relation Test_fetal Diagnostico { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.9 0.05 0.1 0.1 0.95 );
}

relation Scr_2_tr Diagnostico { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.7 0.05 0.3 0.3 0.95 );
}

relation Amniocentesis Diagnostico { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.994 0.994 1.0E-4 0.0060 0.0060 0.9999 );
}

relation ECO_2_tr Diagnostico { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.6 0.6 0.045 0.4 0.4 0.955 );
}

}
