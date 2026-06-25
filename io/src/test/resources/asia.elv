
// Bayesian Network
//   Elvira format 

bnet  Asia { 

// Network Properties

title = "Ejemplo de red clasica en la literatura";
author = "Lauritzen y Spiegelhalter";
whochanged = "Jose A. Gamez";
whenchanged = "19/08/99";
version = 1.0;
default node states = (yes, no);

// Network Variables 

node X {
title = "Positive X-ray?";
comment = "Indica si el test de rayos X ha sido positivo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =30;
pos_y =300;
//num-states = 2;
}

node B {
title = "Has bronchitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =400;
pos_y =100;
//num-states = 2;
}

node D {
title = "Dyspnoea?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =250;
pos_y =300;
//num-states = 2;
}

node A {
title = "Visit to Asia?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =30;
pos_y =30;
//num-states = 2;
}

node S {
title = "Smoker?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =299;
pos_y =30;
//num-states = 2;
states = (yes no);
}

node L {
title = "Has lung cancer";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =200;
pos_y =100;
//num-states = 2;
states = (yes no);
}

node T {
title = "Has tuberculosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =30;
pos_y =100;
//num-states = 2;
states = (yes no);
}

node E {
title = "Tuberculosis or cancer";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =99;
pos_y =200;
//num-states = 2;
states = (yes no);
}


// Links of the associated graph:

link A T;
link T E;
link S L;
link S B;
link L E;
link B D;
link E X;
link E D;

//Network Relationships: 

relation A {
comment = "Probabilidades a priori de haber visitado Asia";
values = table (
		[yes] = 0.01,
		[no] = 0.99,
	       	);
}

relation S {
comment = "Probabilidades a priori de ser fumador";
values = table (
		[yes]=0.5,
		[no]=0.5,
		);
}

relation T A {
comment = "P(T|A)";
values = table(
		[yes,yes]=0.05,
		[yes,no]=0.01,
		[no,yes]=0.95,
		[no,no]=0.99,
		);
}

relation L S {
comment = "P(L|S)";
values = table(
		[yes,yes]=0.1,
		[yes,no]=0.01,
		[no,yes]=0.9,
		[no,no]=0.99,
		);
}
	
relation B S {
comment = "P(B|S)";
values = table(
		[yes,yes]=0.6,
		[yes,no]=0.3,
		[no,yes]=0.4,
		[no,no]=0.7,
		);
}

relation E L T {
comment = "P(E|L,T)";
values = table(
		[yes,yes,yes]=1.0,
		[yes,yes,no]=1.0,
		[yes,no,yes]=1.0,
		[yes,no,no]=0.0,
		[no,yes,yes]=0.0,
		[no,yes,no]=0.0,
		[no,no,yes]=0.,
		[no,no,no]=1.0,
		);
}

relation X E {
comment = "P(X|E)";
values = table(
		[yes,yes]=0.98,
		[yes,no]=0.05,
		[no,yes]=0.02,
		[no,no]=0.95,
		);
}

relation D E B {
comment = "P(D|E,B)";
values = table(
		[yes,yes,yes]=0.9,
		[yes,yes,no]=0.7,
		[yes,no,yes]=0.8,
		[yes,no,no]=0.1,
		[no,yes,yes]=0.1,
		[no,yes,no]=0.3,
		[no,no,yes]=0.2,
		[no,no,no]=0.9,
		);
}




}
