//	   Network
//	   Elvira format

bnet "F:\Bases de datos\Agustín\AGC-D-T-BM-G-29-4-08.xls" {

//		 Network Properties

default node states = ("Presente" , "Ausente");

// Variables

node pleuvisp(finite-states) {
title = "pleuvisp";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =562;
pos_y =287;
num-states = 3;
states = ("1.0" "2.0" "?");
}

node pleupa_p(finite-states) {
title = "pleupa_p";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =494;
pos_y =430;
num-states = 3;
states = ("1.0" "2.0" "?");
}

node mortal_c(finite-states) {
title = "mortal_c";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =290;
pos_y =285;
num-states = 2;
states = ("1.0" "2.0");
}

node sexo(finite-states) {
title = "sexo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =719;
pos_y =134;
num-states = 3;
states = ("1.0" "2.0" "?");
}

node pared_p(finite-states) {
title = "pared_p";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =401;
pos_y =122;
num-states = 3;
states = ("1.0" "2.0" "0.0");
}

//		 Links of the associated graph:

link pleuvisp pleupa_p;

link pared_p pleuvisp;

link pared_p pleupa_p;

link pared_p mortal_c;

//		Network Relationships:

relation pleuvisp pared_p {
values = table(0.6585365853658537 0.024390243902439025 0.3170731707317073 6.108735491753207E-4 6.108735491753207E-4 0.9987782529016493 0.002550428935775562 0.08972872710410387 0.9077208439601205);
}

relation pleupa_p pared_p pleuvisp {
values = table(0.9259259259259259 0.3333333333333333 0.8181818181818182 0.037037037037037035 0.3333333333333333 0.09090909090909091 0.037037037037037035 0.3333333333333333 0.09090909090909091 0.3333333333333333 0.3333333333333333 0.003236245954692557 0.3333333333333333 0.3333333333333333 0.003236245954692557 0.3333333333333333 0.3333333333333333 0.9935275080906149 0.17647058823529413 6.108735491753207E-4 
7.501875468867217E-4 0.058823529411764705 6.108735491753207E-4 0.0967741935483871 0.7647058823529411 0.9987782529016493 0.9024756189047262);
}

relation pared_p {
values = table(0.006182121971595656 0.06466165413533835 0.929156223893066);
}

relation mortal_c pared_p {
values = table(0.002079002079002079 0.11434511434511435 0.8835758835758836 0.006718721627020156 0.06046849464318141 0.9328127837297985);
}

relation sexo {
values = table(0.0015037593984962407 0.0733500417710944 0.9251461988304094);
}

}

