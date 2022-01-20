# Satispay Auth Test

Creates a Signed HTTP message (GET or POST) to providing a signature for the Satispay Signature Test API.

# Requirements

Java > 1.8 is mandatory to run the project. If you want also to check and debug the code, you can use an IDE like Eclipse or IntelliJ.

# Installation 🛠️

You can clone the project from this link:

```sh
git clone https://github.com/valerio65xz/satispay-auth-test.git
```

# Usage ℹ️

If you want to just execute the project, open a terminal in your installation folder and type:

```sh
java -jar satispayauthtest.jar
```

then if you use Postman, you may import `SatispayTest.postman_collection.json`. You can try 4 different combinations:
* GET witout a body
* GET with a body (the behaviour is equal to GET without a body)
* POST without a body
* POST with a body

if you don't have or don't want to use postman:
* The URL to call: `http://localhost:8080/satispay/authenticate`
* Params: `type` (has to be only GET or POST)
* Optional body: you can put whatever you want

# Docs 📚

You can find the detailed documentation in `Documentazione.pdf` or the standard javadoc in `javadoc` folder.
