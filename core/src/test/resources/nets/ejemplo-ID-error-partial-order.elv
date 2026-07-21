// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node D0(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =781;
pos_y =114;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node X1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =552;
pos_y =26;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =869;
pos_y =264;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node X3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =383;
pos_y =57;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D4(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =601;
pos_y =166;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U0(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =605;
pos_y =471;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D0 D2;

link D0 U0;

link D2 U0;

link D4 D0;

link D4 D2;

link D4 U0;

link X1 D0;

link X1 D2;

link X1 U0;

link X1 X3;

link X3 D0;

link X3 D4;

link X3 U0;

//Network Relationships: 

relation X1 { 
comment = "";
deterministic=false;
values= table (0.30940276737672084 0.6905972326232792 );
}

relation X3 X1 { 
comment = "";
deterministic=false;
values= table (0.18445602600338373 0.08163895104086892 0.8155439739966163 0.9183610489591312 );
}

relation U0 D0 D2 D4 X1 X3 { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (2.0 10.0 4.0 2.0 3.0 4.0 123.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 );
}

}
