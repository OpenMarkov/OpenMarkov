// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
title = "Enfermedad";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =154;
pos_y =126;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
title = "Prueba";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =411;
pos_y =210;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Terapia";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =234;
pos_y =209;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("sí" "no");
}

node U(continuous) {
title = "Utilidad";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =183;
pos_y =440;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A B;

link A U;

link B D;

link D U;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.08 0.92 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.75 0.04 0.25 0.96 );
}

relation U A D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (90.0 30.0 90.0 100.0 );
}

}
