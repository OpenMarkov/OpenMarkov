// Bayesian Network
//   Elvira format 

bnet "Bayesian net" { 

// Network Properties

kindofgraph = "directed";
title = "Test bayes net";
author = "jmendoza";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
title = "A";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =47;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node B(finite-states) {
title = "B";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =178;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node C(finite-states) {
title = "C";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =304;
pos_y =46;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node D(finite-states) {
title = "D";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =110;
pos_y =144;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node E(finite-states) {
title = "E";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =241;
pos_y =143;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node F(finite-states) {
title = "F";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =178;
pos_y =247;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

// Links of the associated graph:

link A B;

link A D;

link B C;

link B D;

link B E;

link C E;

link D E;

link D F;

link E F;

//Network Relationships: 

relation A { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 );
}

relation D A B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation F D E { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation B A { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation C B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation E B C D { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

}
