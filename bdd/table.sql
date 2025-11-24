create database chat_ofm;
\c chat_ofm;


CREATE TABLE utilisateur (
    id VARCHAR(255) PRIMARY KEY,
    pseudo VARCHAR(255),
    identifiant VARCHAR(255),
    mdp TEXT,
    date timestamp
);

create sequence seq_utilisateur;

CREATE TABLE bot (
    id VARCHAR(255) PRIMARY KEY,
    idUser INTEGER,
    pseudo VARCHAR(255),
    promptSys TEXT,
    date timestamp
);

create sequence seq_bot;

CREATE TABLE discussion (
    id VARCHAR(255) PRIMARY KEY,
    idUser VARCHAR(255),
    idBot VARCHAR(255),
    date timestamp
);

create sequence seq_discussion;

CREATE TABLE message (
    id VARCHAR(255) PRIMARY KEY,
    idDiscussion VARCHAR(255),
    idEmmeteur VARCHAR(255),
    contenue TEXT,
    date timestamp
);

create sequence seq_message;
