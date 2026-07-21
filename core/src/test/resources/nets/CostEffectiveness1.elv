// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =209;
pos_y =160;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =342;
pos_y =159;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node E(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =261;
pos_y =250;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =404;
pos_y =250;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D C;

link D E;

link X E;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.8 );
}

relation C D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (8.0 0.0 );
}

relation E D X { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (8.0 9.0 0.0 10.0 );
}

}
