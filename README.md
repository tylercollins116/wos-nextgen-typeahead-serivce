1p-typeahead service
====================
To run locally use gradlew runServer command

**REST API**

| Operation | HTTP action | Headers | Query parameters |
| ------------- | ------------- | ------------- | ------------- |
| List of suggestions for given query | **GET** /suggest/search  | **X-1P-User**(required): User authentication token | **query**(required): User input word<br/>**source**(required): Elastic index<br/>**offset**(default: 0): Offset from the first result you want to fetch<br/>**size**(default: 10): Maximum amount of hits to be returned<br/>**highlight**(default: false): Highlights hits inside context|

**Example for NP fields**

| HTTP action | Output | Description |
| ------------- | ------------- | ------------- |
| /suggest/search?query=am&offset=0&source=products&size=1<br/>Headers:<br/>X-1P-User: user_token| [<br/>{<br/>"source": "products",<br/>"took": "392:836",<br/>"hits": 836,<br/>"suggestions": [<br/>{<br/>"keyword": "am",<br/>"info": [<br/>{<br/>"key": "uid",<br/>"value": "17619"<br/>},<br/>{<br/>"key": "name",<br/>"value": "(aminomethyl)-benzoic acid (4-)"<br/>}]}]}] | In this request we are trying to get all hits of 'am' in 'products'.<br/>As you can see total number of hits is 836, but we limited suggestions size using size param,<br/> so we've got only one and first (because offset is 0) hit of 'am' keyword.

