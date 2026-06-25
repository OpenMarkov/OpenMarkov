// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "Artroplastia Total de Rodilla" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node IMC(finite-states) {
title = "IMC";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =121;
pos_y =22;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node Diabetes(finite-states) {
title = "Diabetes";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =267;
pos_y =21;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node Alergia_a_Antibióticos(finite-states) {
title = "Alergia ATB";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =406;
pos_y =22;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node Infeccion_Protesis_Total_de_Rodilla(finite-states) {
title = "Infeccion PTR";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =352;
pos_y =151;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node Analítica_VSG(finite-states) {
title = "VSG";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =343;
pos_y =226;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("valor > 30 mm/h tras 6 meses" "valor =< 30 mm/h tras 6 meses");
}

node Analítica_PCR(finite-states) {
title = "PCR";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =214;
pos_y =226;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("valor > 10 mg/l tras 3 semanas" "valor =< 10 mg/l tras 3 semanas");
}

node Gammagrafía_Galio_67_y_Tecnecio_99(finite-states) {
title = "Ga67 Tc99";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =636;
pos_y =343;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("Positivo" "Negativo" "Prueba no realizada");
}

node Exploración_Movilidad(finite-states) {
title = "Movilidad";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =527;
pos_y =223;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("ROM =< 65" "ROM > 65");
}

node Biopsia_Sinovial_Cortes_Congelados(finite-states) {
title = "Cortes Congelados";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =621;
pos_y =440;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("> 5 PMFN" "=< 5 PMFN" "Prueba no realizada");
}

node Realizar_Implante(finite-states) {
title = "Realizar Implante";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =190;
pos_y =78;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node EVAC_Implante(continuous) {
title = "EVAC Implante";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =97;
pos_y =427;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
unit = "QUALYs";
}

node Realizar_Gammagrafias(finite-states) {
title = "Realizar Gammagrafias";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =360;
pos_y =318;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node Realizar_Biopsia_Sinovial(finite-states) {
title = "Realizar Biopsia Sinovial";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =721;
pos_y =391;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node Tratar_Infeccion_PTR(finite-states) {
title = "Tratar Infeccion PTR";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =1064;
pos_y =465;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("si" "no");
}

node Molestias_Gammagrafía(continuous) {
title = "Molestias Gammagrafía";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =342;
pos_y =453;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
unit = "QUALYs";
}

node Molestias_Biopsia_Sinovial(continuous) {
title = "Molestias Biopsia Sinovial";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =684;
pos_y =547;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
unit = "QUALYs";
}

node EVAC_Total(continuous) {
title = "EVAC Total";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =326;
pos_y =679;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Isquemia(finite-states) {
title = "Isquemia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =433;
pos_y =105;
relevance = 7.0;
purpose = "Riskfactor";
num-states = 3;
states = ("Mayor 1 hora y media" "Menor 1 hora y media" "Implante no realizado");
}

node CC_Drenaje(finite-states) {
title = "CC_Drenaje";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =557;
pos_y =102;
relevance = 7.0;
purpose = "Riskfactor";
num-states = 3;
states = ("menor 800 cc o mayor 1000 cc" "mayor 800 cc y menor 1000 cc" "Implante no realizado");
}

node C_Implante(continuous) {
title = "Coste Implante";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =220;
pos_y =493;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C_Tratamiento(continuous) {
title = "Coste Tratamiento";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =1064;
pos_y =670;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C_Gammagrafia(continuous) {
title = "Coste Gammagrafia";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =479;
pos_y =500;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C_Biopsia_Sinovial(continuous) {
title = "Coste Biopsia Sinovial";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =747;
pos_y =606;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Coste_total(continuous) {
title = "Coste total";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =658;
pos_y =692;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Coste_ajustado(continuous) {
title = "Coste ajustado";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =470;
pos_y =745;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
unit = "QUALYs";
}

node Beneficio_neto(continuous) {
title = "Beneficio neto";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =328;
pos_y =799;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
unit = "QUALYs";
}

node Constante_ajuste(continuous) {
title = "Lambda-inv";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =742;
pos_y =744;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node N_Insignificante(finite-states) {
title = ".";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =817;
pos_y =744;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Mejora_Tratamiento(continuous) {
title = "Mejora Tratamiento";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =931;
pos_y =634;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link Alergia_a_Antibióticos Infeccion_Protesis_Total_de_Rodilla;

link Alergia_a_Antibióticos Realizar_Implante;

link Analítica_PCR Realizar_Gammagrafias;

link Analítica_VSG Realizar_Gammagrafias;

link Biopsia_Sinovial_Cortes_Congelados Tratar_Infeccion_PTR;

link CC_Drenaje Infeccion_Protesis_Total_de_Rodilla;

link CC_Drenaje Realizar_Gammagrafias;

link C_Biopsia_Sinovial Coste_total;

link C_Gammagrafia Coste_total;

link C_Implante Coste_total;

link C_Tratamiento Coste_total;

link Constante_ajuste Coste_ajustado;

link Coste_ajustado Beneficio_neto;

link Coste_total Coste_ajustado;

link Diabetes Infeccion_Protesis_Total_de_Rodilla;

link Diabetes Realizar_Implante;

link EVAC_Implante EVAC_Total;

link EVAC_Total Beneficio_neto;

link Exploración_Movilidad Realizar_Gammagrafias;

link Gammagrafía_Galio_67_y_Tecnecio_99 Realizar_Biopsia_Sinovial;

link IMC Diabetes;

link IMC Infeccion_Protesis_Total_de_Rodilla;

link IMC Realizar_Implante;

link Infeccion_Protesis_Total_de_Rodilla Analítica_PCR;

link Infeccion_Protesis_Total_de_Rodilla Analítica_VSG;

link Infeccion_Protesis_Total_de_Rodilla Biopsia_Sinovial_Cortes_Congelados;

link Infeccion_Protesis_Total_de_Rodilla Exploración_Movilidad;

link Infeccion_Protesis_Total_de_Rodilla Gammagrafía_Galio_67_y_Tecnecio_99;

link Infeccion_Protesis_Total_de_Rodilla Mejora_Tratamiento;

link Isquemia Infeccion_Protesis_Total_de_Rodilla;

link Isquemia Realizar_Gammagrafias;

link Mejora_Tratamiento EVAC_Total;

link Molestias_Biopsia_Sinovial EVAC_Total;

link Molestias_Gammagrafía EVAC_Total;

link N_Insignificante Constante_ajuste;

link Realizar_Biopsia_Sinovial Biopsia_Sinovial_Cortes_Congelados;

link Realizar_Biopsia_Sinovial C_Biopsia_Sinovial;

link Realizar_Biopsia_Sinovial Molestias_Biopsia_Sinovial;

link Realizar_Biopsia_Sinovial Tratar_Infeccion_PTR;

link Realizar_Gammagrafias C_Gammagrafia;

link Realizar_Gammagrafias Gammagrafía_Galio_67_y_Tecnecio_99;

link Realizar_Gammagrafias Molestias_Gammagrafía;

link Realizar_Gammagrafias Realizar_Biopsia_Sinovial;

link Realizar_Implante CC_Drenaje;

link Realizar_Implante C_Implante;

link Realizar_Implante EVAC_Implante;

link Realizar_Implante Infeccion_Protesis_Total_de_Rodilla;

link Realizar_Implante Isquemia;

link Realizar_Implante Realizar_Gammagrafias;

link Tratar_Infeccion_PTR C_Tratamiento;

link Tratar_Infeccion_PTR Mejora_Tratamiento;

//Network Relationships: 

relation IMC { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 );
}

relation Alergia_a_Antibióticos { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 );
}

relation Analítica_PCR Infeccion_Protesis_Total_de_Rodilla { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.89 0.26 0.11 0.74 );
}

relation Analítica_VSG Infeccion_Protesis_Total_de_Rodilla { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.82 0.15 0.18 0.85 );
}

relation Exploración_Movilidad Infeccion_Protesis_Total_de_Rodilla { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.85 0.3 0.15 );
}

relation Diabetes IMC { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation Gammagrafía_Galio_67_y_Tecnecio_99 Infeccion_Protesis_Total_de_Rodilla Realizar_Gammagrafias { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.33 0.0 0.14 0.0 0.67 0.0 0.86 0.0 0.0 1.0 0.0 1.0 );
}

relation Biopsia_Sinovial_Cortes_Congelados Infeccion_Protesis_Total_de_Rodilla Realizar_Biopsia_Sinovial { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.84 0.0 0.05 0.0 0.16 0.0 0.95 0.0 0.0 1.0 0.0 1.0 );
}

relation Molestias_Gammagrafía Realizar_Gammagrafias { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.0 0.0 );
}

relation Molestias_Biopsia_Sinovial Realizar_Biopsia_Sinovial { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-9.20547945205E-6 0.0 );
}

relation C_Implante Realizar_Implante { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (6865.52 0.0 );
}

relation C_Tratamiento Tratar_Infeccion_PTR { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (39196.89 0.0 );
}

relation C_Gammagrafia Realizar_Gammagrafias { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (335.08 0.0 );
}

relation C_Biopsia_Sinovial Realizar_Biopsia_Sinovial { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (405.28 0.0 );
}

relation Beneficio_neto Coste_ajustado EVAC_Total { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation Coste_ajustado Coste_total Constante_ajuste { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation N_Insignificante { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 );
}

relation Constante_ajuste N_Insignificante { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-3.33E-5 -3.33E-5 );
}

relation Infeccion_Protesis_Total_de_Rodilla Alergia_a_Antibióticos CC_Drenaje Diabetes Isquemia IMC Realizar_Implante { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.989553636 0.0 0.957221534 0.0 0.737303454 0.0 0.398672364 0.0 0.0 0.0 0.0 0.0 0.973557904 0.0 0.896877344 0.0 0.521736292 0.0 0.204891176 0.0 0.0 0.0 0.0 0.0 0.950168878 0.0 0.818319025 0.0 0.361006112 0.0 0.11774083 0.0 0.0 0.0 0.0 0.0 0.881111699 0.0 0.63645254 0.0 0.180051295 0.0 0.049312866 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.9505463 0.0 0.819505381 0.0 0.362853602 0.0 0.118574398 0.0 0.0 0.0 0.0 0.0 0.881947178 0.0 0.638301558 0.0 0.181235382 0.0 0.04968927 0.0 0.0 0.0 0.0 0.0 0.794619659 0.0 0.477515175 0.0 0.102845521 0.0 0.026364977 0.0 0.0 0.0 0.0 0.0 0.60060822 0.0 0.262115899 0.0 0.042655789 0.0 0.010415398 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.010446364 1.0 0.042778466 1.0 0.262696546 1.0 0.601327636 1.0 1.0 1.0 1.0 1.0 0.026442096 1.0 0.103122656 1.0 0.478263708 1.0 0.795108824 1.0 1.0 1.0 1.0 1.0 0.049831122 1.0 0.181680975 1.0 0.638993888 1.0 0.88225917 1.0 1.0 1.0 1.0 1.0 0.118888301 1.0 0.36354746 1.0 0.819948705 1.0 0.950687134 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0494537 1.0 0.180494619 1.0 0.637146398 1.0 0.881425602 1.0 1.0 1.0 1.0 1.0 0.118052822 1.0 0.361698442 1.0 0.818764618 1.0 0.95031073 1.0 1.0 1.0 1.0 1.0 0.205380341 1.0 0.522484825 1.0 0.897154479 1.0 0.973635023 1.0 1.0 1.0 1.0 1.0 0.39939178 1.0 0.737884101 1.0 0.957344211 1.0 0.989584602 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 );
}

relation EVAC_Implante Realizar_Implante { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (4.64 0.0 );
}

relation Coste_total C_Biopsia_Sinovial C_Gammagrafia C_Implante C_Tratamiento { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation EVAC_Total EVAC_Implante Molestias_Gammagrafía Molestias_Biopsia_Sinovial Mejora_Tratamiento { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation CC_Drenaje Realizar_Implante { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.84946237 0.0 0.15053763 0.0 0.0 1.0 );
}

relation Isquemia Realizar_Implante { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.62724014 0.0 0.37275986 0.0 0.0 1.0 );
}

relation Mejora_Tratamiento Infeccion_Protesis_Total_de_Rodilla Tratar_Infeccion_PTR { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-4.6419231 -14.4 -4.64 0.0 );
}

}
