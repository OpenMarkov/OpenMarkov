// Influence Diagram
//   Elvira format 

idiagram  "Influence diagram" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
title = "Enfermedad";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =131;
pos_y =75;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
title = "Prueba";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =323;
pos_y =153;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Tratamiento";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =405;
pos_y =294;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
title = "Utilidad";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =131;
pos_y =402;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D U;

link X U;

link X Y;

link Y D;

//Network Relationships: 

relation X { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Y X { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation U X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 0.0 0.0 0.0 );
}

}
