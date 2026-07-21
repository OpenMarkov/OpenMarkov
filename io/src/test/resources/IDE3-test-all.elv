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
title = "Disease";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =104;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Therapy";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =454;
pos_y =220;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
title = "Health state";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =281;
pos_y =338;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Y(finite-states) {
title = "Result of test";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =309;
pos_y =128;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

// Links of the associated graph:

link D U;

link X U;

link X Y;

link Y D;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.14|range(0.1,0.5)|"prevalence de X" # );
}

relation U X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (7.8 2.8 8.8 9.8 );
}

relation Y X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.91 0.03 0.09 0.97 );
}

}
