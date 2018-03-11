# Kotlin One-Time Password Library

[![Build Status](https://travis-ci.org/marcelkliemannel/kotlin-onetimepassword.svg?branch=master)](https://travis-ci.org/marcelkliemannel/kotlin-onetimepassword)

This is a Kotlin one-time password library to generate codes for:
- Google Authenticator
- Time-based One-time Password (TOTP)
- HMAC-based One-time Password (HOTP)

The implementations are based on the RFCs:
-  RFC 4226 [HOTP: An HMAC-Based One-Time Password Algorithm](https://www.ietf.org/rfc/rfc4226.txt)
-  RFC 6238 [TOTP: Time-Based One-Time Password Algorithm](https://tools.ietf.org/html/rfc6238)

## Dependency

The library is available at [Maven Central](https://mvnrepository.com/artifact/com.marcelkliemannel/kotlin-onetimepassword):

### Gradle

```gradle
compile 'com.marcelkliemannel:kotlin-onetimepassword:1.0.0'
```

### Apache Maven

```xml
<dependency>
    <groupId>com.marcelkliemannel</groupId>
    <artifactId>kotlin-onetimepassword</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Number of Code Digits

All three one-time password generators are creating a code value with a fixed length given by the ```codeDigits``` property in the configuration instance. To meet this requirement, the original computed code number gets zeros added to the beginning (and therefore it is represented as a string). The RFC 4226 requires a code digits value between 6 and 8, to assure a good security trade-off. However, this library does not set any requirement for this property. But notice that through the design of the algorithm the maximum code value is 2_147_483_647. Which means that a larger code digits value than 10 just adds more trailing zeros to the code (and in case of 10 digits the first number is 0, 1 or 2).

### HMAC-based One-time Password (HOTP)

The HOTP generator is available through the class ```HmacOneTimePasswordGenerator```.  The constructor takes the secret and a configuration instance of the class ```HmacOneTimePasswordConfig``` as arguments:

```kotlin
val secret = "Leia"
val config = HmacOneTimePasswordConfig(codeDigits = 8, hmacAlgorithm = HmacAlgorithm.SHA1)
val hmacOneTimePasswordGenerator = HmacOneTimePasswordGenerator(secret.toByteArray(), config)
```

The configuration instance takes the number of code digits to be generated (see previous chapter) and the HMAC algorithm to be used (currently are *SHA1*, *SHA256* and *SHA512* available).

The method ```generate(counter: Int)``` can now be used on the generator instance to generate a HOTP code:

```kotlin
hmacOneTimePasswordGenerator.generate(counter = 0)
hmacOneTimePasswordGenerator.generate(counter = 1)
hmacOneTimePasswordGenerator.generate(counter = 2)
...
```

There is also a helper method ```isValid(code: String, counter: Int)``` available on the generator instance, to make the validation of the received code possible in one line.

### Time-based One-time Password (TOTP)

The TOTP generator is available through the class ```TimeBasedOneTimePasswordGenerator```. The constructor takes the secret and a configuration instance of the class ```TimeBasedOneTimePasswordConfig``` as arguments:

```kotlin
val secret = "Leia"
val config = TimeBasedOneTimePasswordConfig(codeDigits = 8, hmacAlgorithm = HmacAlgorithm.SHA1,
                                            timeStep = 30, timeStepUnit = TimeUnit.SECONDS)
val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(secret.toByteArray(), config)
```

As well as the HOTP configuration, the TOTP configuration takes the number of code digits and the HMAC algorithm as arguments (see the previous chapter). Additionally, the time window in which the generated code is valid is represented through the arguments ```timeStep``` and ```timeStepUnit```.

The method ```generate(timestamp: Date)``` can now be used on the generator instance to generate a TOTP code. The default timestamp value is the current system time.

```kotlin
timeBasedOneTimePasswordGenerator.generate() // Will use System.currentTimeMillis()
timeBasedOneTimePasswordGenerator.generate(timestamp = Date(59))
timeBasedOneTimePasswordGenerator.generate(timestamp = Date(1234567890))
...
```

Again, there is a helper method ```isValid(code: String, timestamp: Date)``` available on the generator instance, to make the validation of the received code possible in one line.

### Google Authenticator

The Google Authenticator generator is available through the class ```GoogleAuthenticator```. It is a decorator for the TOTP generator with a fixed code digits value of 6, SHA1 as HMAC algorithm and a time window of 30 seconds. The constructor just takes the secret as an argument. **Notice that the secret must be Base32-encoded!**

```kotlin
val googleAuthenticator = GoogleAuthenticator(secret = "J52XEU3IMFZGKZCTMVRXEZLU") // "OurSharedSecret" Base32-encoded
```

See the TOTP generator for the code generation ```generator(timestamp: Date)``` and validation ```isValid(code: String, timestamp: Date)``` methods.

There is also a helper method ```GoogleAuthenticator.createRandomSecret()``` which will return a 16-byte Base32-decoded random secret.

### Random Secret Generator

RFC 4226 recommends using a secret of the same size as the hash produced by the HMAC algorithm. To make this easy, there is a ```RandomSecretGenerator``` class, to generate secure random secrets with the given length:

```kotlin
val randomSecretGenerator = RandomSecretGenerator()

randomSecretGenerator.createRandomSecret(HmacAlgorithm.SHA1) // 20-byte secret
randomSecretGenerator.createRandomSecret(HmacAlgorithm.SHA256) // 32-byte secret
randomSecretGenerator.createRandomSecret(HmacAlgorithm.SHA512) // 64-byte secret

randomSecretGenerator.createRandomSecret(1234) // 1234 Bytes secret
```

## License

**MIT License**

> Copyright 2018 Marcel Kliemannel
> 
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
> 
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
> 
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
