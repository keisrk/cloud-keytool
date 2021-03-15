#############
cloud-keytool
#############

Extend ``keytool`` to store, manage and retrieve secrets from a cloud secrets
management service.

Usage
=====

Given a valid PKCS#12 key stored in AWS secrets manager, ``cloud-keytool``
synchronizes it with the local keystore.

.. code:: sh

   $ cloud-keytool -list --keystore codekata.net.p12 --storepass <storepass> --profile <profile>
