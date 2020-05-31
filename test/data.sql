--
-- PostgreSQL database dump
--

-- Dumped from database version 12.1 (Debian 12.1-1.pgdg100+1)
-- Dumped by pg_dump version 12.3

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
-- Data for Name: registered_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.registered_user (username, password, name, created_at, updated_at) FROM stdin;
brian	bcrypt+sha512$099e5bfbc6f5c34cb69bcdfac35fa5fd$12$b9dd361b53104d977d67263a792be22756860804a22507a4	Brian	2020-05-23 23:05:41.620825	2020-05-23 23:05:41.620825
brian2	bcrypt+sha512$348fccd3d7b120e20395c5d70d05c9bc$12$ad84de7a5f2ce182292f56ef409c06d3488ceb8f6e87be91	Brian	2020-05-23 23:08:55.170933	2020-05-23 23:08:55.170933
\.


--
-- Data for Name: session; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.session (session_id, created_at, updated_at) FROM stdin;
1	2020-05-30 21:17:44.333759	2020-05-30 21:17:44.333759
2	2020-05-30 21:17:44.333759	2020-05-30 21:17:44.333759
\.


--
-- Data for Name: many_session_has_many_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.many_session_has_many_user (session_id_session, username_registered_user, joined_at) FROM stdin;
1	brian	2020-05-30 21:18:46.678322
1	brian2	2020-05-30 21:18:57.16302
2	brian2	2020-05-30 21:19:06.64684
2	brian	2020-05-30 21:19:16.869241
\.


--
-- Data for Name: recipe; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recipe (recipe_id, name, description, image, created_at, updated_at) FROM stdin;
1	recipe 1	descr	\N	2020-05-27 20:05:42.333848	2020-05-27 20:05:42.333848
2	recipe 2	description stuff	\N	2020-05-27 20:05:42.333848	2020-05-27 20:05:42.333848
3	no description	\N	\N	2020-05-27 20:05:42.333848	2020-05-27 20:05:42.333848
4	can't edit	brian has but can't edit	\N	2020-05-27 20:06:52.36044	2020-05-27 20:06:52.36044
\.


--
-- Data for Name: session_recipes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.session_recipes (session_id_session, recipe_id_recipe) FROM stdin;
\.


--
-- Data for Name: many_session_recipes_has_many_registered_user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.many_session_recipes_has_many_registered_user (session_id_session_session_recipes, recipe_id_recipe_session_recipes, username_registered_user, likes) FROM stdin;
\.


--
-- Data for Name: many_user_has_many_recipe; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.many_user_has_many_recipe (username_registered_user, recipe_id_recipe, can_edit) FROM stdin;
brian	1	t
brian	2	t
brian	3	t
brian	4	f
brian2	4	t
\.


--
-- Name: recipe_recipe_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recipe_recipe_id_seq', 4, true);


--
-- Name: session_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.session_session_id_seq', 2, true);


--
-- PostgreSQL database dump complete
--

