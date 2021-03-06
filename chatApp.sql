PGDMP     ;                    z           chatDB    14.2    14.2     ?           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            ?           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            ?           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            ?           1262    16394    chatDB    DATABASE     l   CREATE DATABASE "chatDB" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'English_United States.1252';
    DROP DATABASE "chatDB";
                postgres    false            ?            1259    16414    messages    TABLE     ?   CREATE TABLE public.messages (
    message_id bigint NOT NULL,
    author_id integer NOT NULL,
    recipient_id integer NOT NULL,
    message bytea
);
    DROP TABLE public.messages;
       public         heap    postgres    false            ?            1259    16454    messages_message_id_seq    SEQUENCE     ?   CREATE SEQUENCE public.messages_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.messages_message_id_seq;
       public          postgres    false    210                        0    0    messages_message_id_seq    SEQUENCE OWNED BY     S   ALTER SEQUENCE public.messages_message_id_seq OWNED BY public.messages.message_id;
          public          postgres    false    212            ?            1259    16407    users    TABLE     ?   CREATE TABLE public.users (
    user_id bigint NOT NULL,
    username character(16),
    public_key bytea,
    encrypted_privatekey bytea
);
    DROP TABLE public.users;
       public         heap    postgres    false            ?            1259    16447    users_user_id_seq    SEQUENCE     z   CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.users_user_id_seq;
       public          postgres    false    209                       0    0    users_user_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;
          public          postgres    false    211            b           2604    16455    messages message_id    DEFAULT     z   ALTER TABLE ONLY public.messages ALTER COLUMN message_id SET DEFAULT nextval('public.messages_message_id_seq'::regclass);
 B   ALTER TABLE public.messages ALTER COLUMN message_id DROP DEFAULT;
       public          postgres    false    212    210            a           2604    16448    users user_id    DEFAULT     n   ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);
 <   ALTER TABLE public.users ALTER COLUMN user_id DROP DEFAULT;
       public          postgres    false    211    209            ?          0    16414    messages 
   TABLE DATA           P   COPY public.messages (message_id, author_id, recipient_id, message) FROM stdin;
    public          postgres    false    210   @       ?          0    16407    users 
   TABLE DATA           T   COPY public.users (user_id, username, public_key, encrypted_privatekey) FROM stdin;
    public          postgres    false    209   ]                  0    0    messages_message_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.messages_message_id_seq', 1, false);
          public          postgres    false    212                       0    0    users_user_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.users_user_id_seq', 1, false);
          public          postgres    false    211            h           2606    16462    messages messages_pkey 
   CONSTRAINT     \   ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (message_id);
 @   ALTER TABLE ONLY public.messages DROP CONSTRAINT messages_pkey;
       public            postgres    false    210            d           2606    16453    users users_pkey 
   CONSTRAINT     S   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public            postgres    false    209            e           1259    16468    fki_authors_fkey    INDEX     J   CREATE INDEX fki_authors_fkey ON public.messages USING btree (author_id);
 $   DROP INDEX public.fki_authors_fkey;
       public            postgres    false    210            f           1259    16474    fki_recipient_fkey    INDEX     O   CREATE INDEX fki_recipient_fkey ON public.messages USING btree (recipient_id);
 &   DROP INDEX public.fki_recipient_fkey;
       public            postgres    false    210            i           2606    16463    messages authors_fkey    FK CONSTRAINT     ?   ALTER TABLE ONLY public.messages
    ADD CONSTRAINT authors_fkey FOREIGN KEY (author_id) REFERENCES public.users(user_id) NOT VALID;
 ?   ALTER TABLE ONLY public.messages DROP CONSTRAINT authors_fkey;
       public          postgres    false    210    209    3172            j           2606    16469    messages recipient_fkey    FK CONSTRAINT     ?   ALTER TABLE ONLY public.messages
    ADD CONSTRAINT recipient_fkey FOREIGN KEY (recipient_id) REFERENCES public.users(user_id) NOT VALID;
 A   ALTER TABLE ONLY public.messages DROP CONSTRAINT recipient_fkey;
       public          postgres    false    209    210    3172            ?      x?????? ? ?      ?      x?????? ? ?     