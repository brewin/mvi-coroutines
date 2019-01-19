Experimental Android app using a very simple Clean+MVI/UDF architecture built on Kotlin coroutines.

* Unidirectional data flow (UiEvent → SomeUseCase.Update → UiState)
* State survives configuration changes and process death