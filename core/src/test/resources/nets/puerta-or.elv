// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =171;
pos_y =171;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =413;
pos_y =171;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =292;
pos_y =338;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A C;

link B C;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.32 0.68 );
}

relation C A B { 
comment = "";
deterministic=false;
values= function  
          Or(CA,CB,CResidual);

henrionVSdiez = "Diez";
}

relation C A { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = CA;
deterministic=false;
values= table (0.8 0.0 0.2 1.0 );
}

relation C B { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = CB;
deterministic=false;
values= table (0.7 0.0 0.3 1.0 );
}

relation C { 
comment = "";
kind-of-relation = potential;
active=false;
name-of-relation = CResidual;
deterministic=false;
values= table (0.0010 0.999 );
}

}
