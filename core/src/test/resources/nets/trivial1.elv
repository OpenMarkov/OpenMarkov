// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =147;
pos_y =110;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =353;
pos_y =201;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =513;
pos_y =295;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node Eff(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =298;
pos_y =410;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D Eff;

link X Eff;

link X Y;

link Y D;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
}

relation Y X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.95 0.3 0.05 0.7 );
}

relation Eff X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (2.0 0.0 1.0 3.0 );
}

}
