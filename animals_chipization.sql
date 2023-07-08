--
-- PostgreSQL database dump
--

-- Dumped from database version 15.0
-- Dumped by pg_dump version 15.0

-- Started on 2023-04-18 18:38:32

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3447 (class 1262 OID 19831)
-- Name: animals_chipization; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE animals_chipization WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';


ALTER DATABASE animals_chipization OWNER TO postgres;

\connect animals_chipization

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 237 (class 1259 OID 21061)
-- Name: a_al_identity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.a_al_identity (
    id_a_al_identity integer NOT NULL,
    id_area integer NOT NULL,
    id_area_location integer NOT NULL
);


ALTER TABLE public.a_al_identity OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 21060)
-- Name: a_al_identity_id_a_al_identity_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.a_al_identity_id_a_al_identity_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.a_al_identity_id_a_al_identity_seq OWNER TO postgres;

--
-- TOC entry 3448 (class 0 OID 0)
-- Dependencies: 236
-- Name: a_al_identity_id_a_al_identity_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.a_al_identity_id_a_al_identity_seq OWNED BY public.a_al_identity.id_a_al_identity;


--
-- TOC entry 229 (class 1259 OID 20950)
-- Name: a_at_identity_id_a_at_identity_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.a_at_identity_id_a_at_identity_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.a_at_identity_id_a_at_identity_seq OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 19952)
-- Name: a_at_identity; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.a_at_identity (
    id_animal integer NOT NULL,
    id_animal_type integer NOT NULL,
    id_a_at_identity integer DEFAULT nextval('public.a_at_identity_id_a_at_identity_seq'::regclass) NOT NULL
);


ALTER TABLE public.a_at_identity OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 19887)
-- Name: animal_genders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.animal_genders (
    id_animal_gender integer NOT NULL,
    gender character varying(255) NOT NULL
);


ALTER TABLE public.animal_genders OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 19886)
-- Name: animal_genders_id_animal_gender_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.animal_genders_id_animal_gender_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.animal_genders_id_animal_gender_seq OWNER TO postgres;

--
-- TOC entry 3449 (class 0 OID 0)
-- Dependencies: 218
-- Name: animal_genders_id_animal_gender_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.animal_genders_id_animal_gender_seq OWNED BY public.animal_genders.id_animal_gender;


--
-- TOC entry 217 (class 1259 OID 19880)
-- Name: animal_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.animal_types (
    id_animal_type integer NOT NULL,
    type character varying(255) NOT NULL
);


ALTER TABLE public.animal_types OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 19879)
-- Name: animal_types_id_animal_type_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.animal_types_id_animal_type_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.animal_types_id_animal_type_seq OWNER TO postgres;

--
-- TOC entry 3450 (class 0 OID 0)
-- Dependencies: 216
-- Name: animal_types_id_animal_type_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.animal_types_id_animal_type_seq OWNED BY public.animal_types.id_animal_type;


--
-- TOC entry 225 (class 1259 OID 19908)
-- Name: animals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.animals (
    id_animal integer NOT NULL,
    weight real NOT NULL,
    length real NOT NULL,
    height real NOT NULL,
    id_animal_gender integer NOT NULL,
    id_animal_life_status integer NOT NULL,
    chipping_date_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_chipper integer NOT NULL,
    id_chipping_location integer NOT NULL,
    death_date_time timestamp with time zone
);


ALTER TABLE public.animals OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 19907)
-- Name: animals_id_animal_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.animals_id_animal_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.animals_id_animal_seq OWNER TO postgres;

--
-- TOC entry 3451 (class 0 OID 0)
-- Dependencies: 224
-- Name: animals_id_animal_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.animals_id_animal_seq OWNED BY public.animals.id_animal;


--
-- TOC entry 221 (class 1259 OID 19894)
-- Name: animals_life_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.animals_life_status (
    id_animal_life_status integer NOT NULL,
    life_status character varying(255) NOT NULL
);


ALTER TABLE public.animals_life_status OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 19893)
-- Name: animals_life_status_id_animal_life_status_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.animals_life_status_id_animal_life_status_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.animals_life_status_id_animal_life_status_seq OWNER TO postgres;

--
-- TOC entry 3452 (class 0 OID 0)
-- Dependencies: 220
-- Name: animals_life_status_id_animal_life_status_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.animals_life_status_id_animal_life_status_seq OWNED BY public.animals_life_status.id_animal_life_status;


--
-- TOC entry 235 (class 1259 OID 21054)
-- Name: area_locations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.area_locations (
    id_area_location integer NOT NULL,
    longitude double precision NOT NULL,
    latitude double precision NOT NULL
);


ALTER TABLE public.area_locations OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 21053)
-- Name: area_locations_id_area_location_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.area_locations_id_area_location_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.area_locations_id_area_location_seq OWNER TO postgres;

--
-- TOC entry 3453 (class 0 OID 0)
-- Dependencies: 234
-- Name: area_locations_id_area_location_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.area_locations_id_area_location_seq OWNED BY public.area_locations.id_area_location;


--
-- TOC entry 233 (class 1259 OID 21047)
-- Name: areas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.areas (
    id_area integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.areas OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 21046)
-- Name: areas_id_area_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.areas_id_area_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.areas_id_area_seq OWNER TO postgres;

--
-- TOC entry 3454 (class 0 OID 0)
-- Dependencies: 232
-- Name: areas_id_area_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.areas_id_area_seq OWNED BY public.areas.id_area;


--
-- TOC entry 223 (class 1259 OID 19901)
-- Name: chipping_locations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chipping_locations (
    id_chipping_location integer NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL
);


ALTER TABLE public.chipping_locations OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 19900)
-- Name: chipping_locations_id_chipping_location_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.chipping_locations_id_chipping_location_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.chipping_locations_id_chipping_location_seq OWNER TO postgres;

--
-- TOC entry 3455 (class 0 OID 0)
-- Dependencies: 222
-- Name: chipping_locations_id_chipping_location_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.chipping_locations_id_chipping_location_seq OWNED BY public.chipping_locations.id_chipping_location;


--
-- TOC entry 231 (class 1259 OID 21020)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    id_user_role integer NOT NULL,
    role character varying(255) NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 21019)
-- Name: user_roles_id_user_role_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_roles_id_user_role_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_roles_id_user_role_seq OWNER TO postgres;

--
-- TOC entry 3456 (class 0 OID 0)
-- Dependencies: 230
-- Name: user_roles_id_user_role_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_roles_id_user_role_seq OWNED BY public.user_roles.id_user_role;


--
-- TOC entry 215 (class 1259 OID 19871)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id_user integer NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    id_user_role integer NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 214 (class 1259 OID 19870)
-- Name: users_id_user_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_user_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_user_seq OWNER TO postgres;

--
-- TOC entry 3457 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_id_user_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_user_seq OWNED BY public.users.id_user;


--
-- TOC entry 227 (class 1259 OID 19936)
-- Name: visited_locations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.visited_locations (
    id_visited_location integer NOT NULL,
    date_time_of_visit_location_point timestamp with time zone NOT NULL,
    id_chipping_location integer NOT NULL,
    id_animal integer NOT NULL
);


ALTER TABLE public.visited_locations OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 19935)
-- Name: visited_locations_id_visited_location_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.visited_locations_id_visited_location_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.visited_locations_id_visited_location_seq OWNER TO postgres;

--
-- TOC entry 3458 (class 0 OID 0)
-- Dependencies: 226
-- Name: visited_locations_id_visited_location_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.visited_locations_id_visited_location_seq OWNED BY public.visited_locations.id_visited_location;


--
-- TOC entry 3240 (class 2604 OID 21064)
-- Name: a_al_identity id_a_al_identity; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_al_identity ALTER COLUMN id_a_al_identity SET DEFAULT nextval('public.a_al_identity_id_a_al_identity_seq'::regclass);


--
-- TOC entry 3230 (class 2604 OID 19890)
-- Name: animal_genders id_animal_gender; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animal_genders ALTER COLUMN id_animal_gender SET DEFAULT nextval('public.animal_genders_id_animal_gender_seq'::regclass);


--
-- TOC entry 3229 (class 2604 OID 19883)
-- Name: animal_types id_animal_type; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animal_types ALTER COLUMN id_animal_type SET DEFAULT nextval('public.animal_types_id_animal_type_seq'::regclass);


--
-- TOC entry 3233 (class 2604 OID 19911)
-- Name: animals id_animal; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals ALTER COLUMN id_animal SET DEFAULT nextval('public.animals_id_animal_seq'::regclass);


--
-- TOC entry 3231 (class 2604 OID 19897)
-- Name: animals_life_status id_animal_life_status; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals_life_status ALTER COLUMN id_animal_life_status SET DEFAULT nextval('public.animals_life_status_id_animal_life_status_seq'::regclass);


--
-- TOC entry 3239 (class 2604 OID 21057)
-- Name: area_locations id_area_location; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.area_locations ALTER COLUMN id_area_location SET DEFAULT nextval('public.area_locations_id_area_location_seq'::regclass);


--
-- TOC entry 3238 (class 2604 OID 21050)
-- Name: areas id_area; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.areas ALTER COLUMN id_area SET DEFAULT nextval('public.areas_id_area_seq'::regclass);


--
-- TOC entry 3232 (class 2604 OID 19904)
-- Name: chipping_locations id_chipping_location; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chipping_locations ALTER COLUMN id_chipping_location SET DEFAULT nextval('public.chipping_locations_id_chipping_location_seq'::regclass);


--
-- TOC entry 3237 (class 2604 OID 21023)
-- Name: user_roles id_user_role; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles ALTER COLUMN id_user_role SET DEFAULT nextval('public.user_roles_id_user_role_seq'::regclass);


--
-- TOC entry 3228 (class 2604 OID 19874)
-- Name: users id_user; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id_user SET DEFAULT nextval('public.users_id_user_seq'::regclass);


--
-- TOC entry 3235 (class 2604 OID 19939)
-- Name: visited_locations id_visited_location; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visited_locations ALTER COLUMN id_visited_location SET DEFAULT nextval('public.visited_locations_id_visited_location_seq'::regclass);


--
-- TOC entry 3441 (class 0 OID 21061)
-- Dependencies: 237
-- Data for Name: a_al_identity; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (5, 2, 5);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (6, 2, 6);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (7, 2, 7);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (1, 1, 1);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (2, 1, 2);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (3, 1, 3);
INSERT INTO public.a_al_identity (id_a_al_identity, id_area, id_area_location) VALUES (4, 1, 4);


--
-- TOC entry 3432 (class 0 OID 19952)
-- Dependencies: 228
-- Data for Name: a_at_identity; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (3, 1, 4);
INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (3, 2, 5);
INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (1, 2, 1);
INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (2, 1, 3);
INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (2, 3, 8);
INSERT INTO public.a_at_identity (id_animal, id_animal_type, id_a_at_identity) VALUES (2, 5, 9);


--
-- TOC entry 3423 (class 0 OID 19887)
-- Dependencies: 219
-- Data for Name: animal_genders; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.animal_genders (id_animal_gender, gender) VALUES (1, 'MALE');
INSERT INTO public.animal_genders (id_animal_gender, gender) VALUES (2, 'FEMALE');
INSERT INTO public.animal_genders (id_animal_gender, gender) VALUES (3, 'OTHER');


--
-- TOC entry 3421 (class 0 OID 19880)
-- Dependencies: 217
-- Data for Name: animal_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.animal_types (id_animal_type, type) VALUES (1, 'amphibian');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (2, 'reptile');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (3, 'half-mammal');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (5, 'half-reptile');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (6, 'half-amphibian');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (7, '0bbcd398-1c42-4663-afad-82a2069c828c');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (8, '61b66bf2-8744-4c62-b7ab-279b76772aac');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (9, '3d1c5778-a2d2-4a44-b4c2-bd861be3cb73');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (10, 'f8acfdeb-8ff3-4c9d-bc00-8309285c9d1b');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (11, 'd3aa7c97-2a5f-4272-8249-d88a3bc588af');
INSERT INTO public.animal_types (id_animal_type, type) VALUES (12, '3550ba3f-d00b-44ec-b7d9-36af8a23a080');


--
-- TOC entry 3429 (class 0 OID 19908)
-- Dependencies: 225
-- Data for Name: animals; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.animals (id_animal, weight, length, height, id_animal_gender, id_animal_life_status, chipping_date_time, id_chipper, id_chipping_location, death_date_time) VALUES (1, 3143.3545, 10.315561, 3.237343, 1, 2, '2023-04-13 23:21:48.270737+07', 1, 1, NULL);
INSERT INTO public.animals (id_animal, weight, length, height, id_animal_gender, id_animal_life_status, chipping_date_time, id_chipper, id_chipping_location, death_date_time) VALUES (2, 3143.3545, 10.315561, 3.237343, 1, 2, '2023-04-13 23:21:53.979768+07', 1, 2, NULL);
INSERT INTO public.animals (id_animal, weight, length, height, id_animal_gender, id_animal_life_status, chipping_date_time, id_chipper, id_chipping_location, death_date_time) VALUES (3, 3143.3545, 10.315561, 3.237343, 1, 2, '2023-04-15 00:05:41.372802+07', 1, 1, NULL);


--
-- TOC entry 3425 (class 0 OID 19894)
-- Dependencies: 221
-- Data for Name: animals_life_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.animals_life_status (id_animal_life_status, life_status) VALUES (1, 'DEAD');
INSERT INTO public.animals_life_status (id_animal_life_status, life_status) VALUES (2, 'ALIVE');


--
-- TOC entry 3439 (class 0 OID 21054)
-- Dependencies: 235
-- Data for Name: area_locations; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (1, 0, -75);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (2, 0, -90);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (3, 15, -90);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (4, 15, -75);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (5, -151, 14);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (6, -164, 14);
INSERT INTO public.area_locations (id_area_location, longitude, latitude) VALUES (7, -157.5, 3);


--
-- TOC entry 3437 (class 0 OID 21047)
-- Dependencies: 233
-- Data for Name: areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.areas (id_area, name) VALUES (1, 'Area_1');
INSERT INTO public.areas (id_area, name) VALUES (2, 'c700f4e3-ce7f-474a-b972-d55f274094ca');


--
-- TOC entry 3427 (class 0 OID 19901)
-- Dependencies: 223
-- Data for Name: chipping_locations; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.chipping_locations (id_chipping_location, latitude, longitude) VALUES (1, -80, 10);
INSERT INTO public.chipping_locations (id_chipping_location, latitude, longitude) VALUES (2, 0, 0);
INSERT INTO public.chipping_locations (id_chipping_location, latitude, longitude) VALUES (3, -75, 0);


--
-- TOC entry 3435 (class 0 OID 21020)
-- Dependencies: 231
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.user_roles (id_user_role, role) VALUES (1, 'ADMIN');
INSERT INTO public.user_roles (id_user_role, role) VALUES (2, 'CHIPPER');
INSERT INTO public.user_roles (id_user_role, role) VALUES (3, 'USER');


--
-- TOC entry 3419 (class 0 OID 19871)
-- Dependencies: 215
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (1, 'adminFirstName', 'adminLastName', 'admin@simbirsoft.com', '$2a$12$0LLQZQ9FXORKRViFi4/LfefIUXsOUM3LS9sn0h9r5SQu.dJNxO4dS', 1);
INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (2, 'chipperFirstName', 'chipperLastName', 'chipper@simbirsoft.com', '$2a$12$m48/xVFMAZetz8WCK0tExe3ieG3tICU7u46qNq7D11nffPlkJzd16', 2);
INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (4, 'Maybelle', 'Heaney', 'scarlet.gulgowski@gmail.com', '$2a$12$EbycMaKYiaRq4tELT7jso.NSw.VW39cUFroxE4xD5HL63cIJxQzxK', 3);
INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (5, 'Gipp', 'Gipddwdqp', 'test@mail.ru', '$2a$12$rdWNmn37UVnPPpqOjH3lXeE2oVJQsRuxpnxwO.JCIr8nhcVRx00cm', 2);
INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (3, 'userFirstName', 'userLastName', 'user@simbirsoft.com', '$2a$12$lka3Q.ixadM63gdstrHCzuovLj40GofqgGX2Q88/R6Nq8B34YGT3q', 3);
INSERT INTO public.users (id_user, first_name, last_name, email, password, id_user_role) VALUES (8, 'Fast', 'Engine', 'engeneer@gmail.com', '$2a$12$xIaZF9iPrLP1vHs3VbovGegZB/FE2x2UD6AIbBo5G2QkhBgsPjUmq', 3);


--
-- TOC entry 3431 (class 0 OID 19936)
-- Dependencies: 227
-- Data for Name: visited_locations; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.visited_locations (id_visited_location, date_time_of_visit_location_point, id_chipping_location, id_animal) VALUES (25, '2023-04-13 23:22:28.148964+07', 2, 1);
INSERT INTO public.visited_locations (id_visited_location, date_time_of_visit_location_point, id_chipping_location, id_animal) VALUES (26, '2023-04-13 23:22:36.022644+07', 3, 2);


--
-- TOC entry 3459 (class 0 OID 0)
-- Dependencies: 236
-- Name: a_al_identity_id_a_al_identity_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.a_al_identity_id_a_al_identity_seq', 7, true);


--
-- TOC entry 3460 (class 0 OID 0)
-- Dependencies: 229
-- Name: a_at_identity_id_a_at_identity_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.a_at_identity_id_a_at_identity_seq', 9, true);


--
-- TOC entry 3461 (class 0 OID 0)
-- Dependencies: 218
-- Name: animal_genders_id_animal_gender_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.animal_genders_id_animal_gender_seq', 3, true);


--
-- TOC entry 3462 (class 0 OID 0)
-- Dependencies: 216
-- Name: animal_types_id_animal_type_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.animal_types_id_animal_type_seq', 12, true);


--
-- TOC entry 3463 (class 0 OID 0)
-- Dependencies: 224
-- Name: animals_id_animal_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.animals_id_animal_seq', 3, true);


--
-- TOC entry 3464 (class 0 OID 0)
-- Dependencies: 220
-- Name: animals_life_status_id_animal_life_status_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.animals_life_status_id_animal_life_status_seq', 2, true);


--
-- TOC entry 3465 (class 0 OID 0)
-- Dependencies: 234
-- Name: area_locations_id_area_location_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.area_locations_id_area_location_seq', 7, true);


--
-- TOC entry 3466 (class 0 OID 0)
-- Dependencies: 232
-- Name: areas_id_area_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.areas_id_area_seq', 2, true);


--
-- TOC entry 3467 (class 0 OID 0)
-- Dependencies: 222
-- Name: chipping_locations_id_chipping_location_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.chipping_locations_id_chipping_location_seq', 3, true);


--
-- TOC entry 3468 (class 0 OID 0)
-- Dependencies: 230
-- Name: user_roles_id_user_role_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_roles_id_user_role_seq', 3, true);


--
-- TOC entry 3469 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_id_user_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_user_seq', 8, true);


--
-- TOC entry 3470 (class 0 OID 0)
-- Dependencies: 226
-- Name: visited_locations_id_visited_location_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.visited_locations_id_visited_location_seq', 26, true);


--
-- TOC entry 3264 (class 2606 OID 21066)
-- Name: a_al_identity a_al_identity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_al_identity
    ADD CONSTRAINT a_al_identity_pkey PRIMARY KEY (id_a_al_identity);


--
-- TOC entry 3256 (class 2606 OID 20949)
-- Name: a_at_identity a_at_identity_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_at_identity
    ADD CONSTRAINT a_at_identity_pk PRIMARY KEY (id_a_at_identity);


--
-- TOC entry 3246 (class 2606 OID 19892)
-- Name: animal_genders animal_genders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animal_genders
    ADD CONSTRAINT animal_genders_pkey PRIMARY KEY (id_animal_gender);


--
-- TOC entry 3244 (class 2606 OID 19885)
-- Name: animal_types animal_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animal_types
    ADD CONSTRAINT animal_types_pkey PRIMARY KEY (id_animal_type);


--
-- TOC entry 3248 (class 2606 OID 19899)
-- Name: animals_life_status animals_life_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals_life_status
    ADD CONSTRAINT animals_life_status_pkey PRIMARY KEY (id_animal_life_status);


--
-- TOC entry 3252 (class 2606 OID 19914)
-- Name: animals animals_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals
    ADD CONSTRAINT animals_pkey PRIMARY KEY (id_animal);


--
-- TOC entry 3262 (class 2606 OID 21059)
-- Name: area_locations area_locations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.area_locations
    ADD CONSTRAINT area_locations_pkey PRIMARY KEY (id_area_location);


--
-- TOC entry 3260 (class 2606 OID 21052)
-- Name: areas areas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.areas
    ADD CONSTRAINT areas_pkey PRIMARY KEY (id_area);


--
-- TOC entry 3250 (class 2606 OID 19906)
-- Name: chipping_locations chipping_locations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chipping_locations
    ADD CONSTRAINT chipping_locations_pkey PRIMARY KEY (id_chipping_location);


--
-- TOC entry 3258 (class 2606 OID 21025)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id_user_role);


--
-- TOC entry 3242 (class 2606 OID 19878)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3254 (class 2606 OID 19941)
-- Name: visited_locations visited_locations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visited_locations
    ADD CONSTRAINT visited_locations_pkey PRIMARY KEY (id_visited_location);


--
-- TOC entry 3274 (class 2606 OID 21067)
-- Name: a_al_identity a_al_identity_id_area_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_al_identity
    ADD CONSTRAINT a_al_identity_id_area_fkey FOREIGN KEY (id_area) REFERENCES public.areas(id_area) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3275 (class 2606 OID 21072)
-- Name: a_al_identity a_al_identity_id_area_location_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_al_identity
    ADD CONSTRAINT a_al_identity_id_area_location_fkey FOREIGN KEY (id_area_location) REFERENCES public.area_locations(id_area_location) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3272 (class 2606 OID 19955)
-- Name: a_at_identity a_at_identity_id_animal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_at_identity
    ADD CONSTRAINT a_at_identity_id_animal_fkey FOREIGN KEY (id_animal) REFERENCES public.animals(id_animal) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3273 (class 2606 OID 19960)
-- Name: a_at_identity a_at_identity_id_animal_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.a_at_identity
    ADD CONSTRAINT a_at_identity_id_animal_type_fkey FOREIGN KEY (id_animal_type) REFERENCES public.animal_types(id_animal_type) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3266 (class 2606 OID 19930)
-- Name: animals animals_id_animal_gender_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals
    ADD CONSTRAINT animals_id_animal_gender_fkey FOREIGN KEY (id_animal_gender) REFERENCES public.animal_genders(id_animal_gender) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3267 (class 2606 OID 19925)
-- Name: animals animals_id_animal_life_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals
    ADD CONSTRAINT animals_id_animal_life_status_fkey FOREIGN KEY (id_animal_life_status) REFERENCES public.animals_life_status(id_animal_life_status) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3268 (class 2606 OID 19920)
-- Name: animals animals_id_chipper_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals
    ADD CONSTRAINT animals_id_chipper_fkey FOREIGN KEY (id_chipper) REFERENCES public.users(id_user) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3269 (class 2606 OID 19915)
-- Name: animals animals_id_chipping_location_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.animals
    ADD CONSTRAINT animals_id_chipping_location_fkey FOREIGN KEY (id_chipping_location) REFERENCES public.chipping_locations(id_chipping_location) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3265 (class 2606 OID 21027)
-- Name: users animals_id_user_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT animals_id_user_role_fkey FOREIGN KEY (id_user_role) REFERENCES public.user_roles(id_user_role) NOT VALID;


--
-- TOC entry 3270 (class 2606 OID 19942)
-- Name: visited_locations visited_locations_id_animal_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visited_locations
    ADD CONSTRAINT visited_locations_id_animal_fkey FOREIGN KEY (id_animal) REFERENCES public.animals(id_animal) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3271 (class 2606 OID 19947)
-- Name: visited_locations visited_locations_id_chipping_location_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visited_locations
    ADD CONSTRAINT visited_locations_id_chipping_location_fkey FOREIGN KEY (id_chipping_location) REFERENCES public.chipping_locations(id_chipping_location) ON UPDATE CASCADE ON DELETE CASCADE;


-- Completed on 2023-04-18 18:38:33

--
-- PostgreSQL database dump complete
--

