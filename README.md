# EOL Notice
model-scoring-java, version v0.74 will no longer be available after Jan 15, 2022 and will not be supported with any additional functional, security, or other updates. All versions are provided as is. Intel recommends that users of daal-tk uninstall and discontinue use as soon as possible.

# model-scoring-java

This repo contains the JVM-based Model Scoring Engine that supports Spark-tk and daal-tk models. 

The Scoring Engine is an application capable of loading trained machine learning models exported by Spark-tk or daal-tk in MAR (Model ARchive) format and using the models to score streams of incoming data. These models implement Model ARchive Interface defined in the ModelArchiver repository at: https://github.com/tapanalyticstoolkit/modelarchiver. Applications can use the Scoring Engine API to get predictions produced by a model.

##scoring-pipelines vs. scoring-engine

If you need to perform transformations on the incoming data you wish to score, use the scoring-pipelines instead of the scoring-engine. The scoring-pipelines perform supported data transformations and automatically submit the output to the scoring engine. The repo for the scoring-pipelines is https://github.com/tapanalyticstoolkit/scoring-pipelines.

##What's new

This is the initial release of the `model-scoring-java` repo.

##Known issues

None.

##Scoring Engine support for revised models

The Scoring Engine allows a revised model of the same type and using the same I/O parameters to be seamlessly updated, without needing to redeploy the Scoring Engine. It also supports forcing the use of a revised model that may be incompatible with the previous revision. Details are [provided below] (https://github.com/tapanalyticstoolkit/model-scoring-java#model-revision).

#Scoring example  

The sample below is a Python script to send requests to the scoring engine containing a trained Random Forest Classifier model:  

```
>>> frame = tc.frame.create([[1,19.8446136104,2.2985856384],[1,16.8973559126,2.6933495054],
...                                 [1,5.5548729596,2.7777687995],[0,46.1810010826,3.1611961917],
...                                 [0,44.3117586448,3.3458963222],[0,34.6334526911,3.6429838715]],
...                                 [('Class', int), ('Dim_1', float), ('Dim_2', float)])

>>> frame.inspect()
[#]  Class  Dim_1          Dim_2
=======================================
[0]      1  19.8446136104  2.2985856384
[1]      1  16.8973559126  2.6933495054
[2]      1   5.5548729596  2.7777687995
[3]      0  46.1810010826  3.1611961917
[4]      0  44.3117586448  3.3458963222
[5]      0  34.6334526911  3.6429838715

>>> model = tc.models.classification.random_forest_classifier.train(frame,
...                                                                ['Dim_1', 'Dim_2'],
...                                                                'Class',
...                                                                num_classes=2,
...                                                                num_trees=1,
...                                                                impurity="entropy",
...                                                                max_depth=4,
...                                                                max_bins=100)

>>> model.feature_importances()
{u'Dim_1': 1.0, u'Dim_2': 0.0}

>>> predicted_frame = model.predict(frame, ['Dim_1', 'Dim_2'])

>>> predicted_frame.inspect()
[#]  Class  Dim_1          Dim_2         predicted_class
========================================================
[0]      1  19.8446136104  2.2985856384                1
[1]      1  16.8973559126  2.6933495054                1
[2]      1   5.5548729596  2.7777687995                1
[3]      0  46.1810010826  3.1611961917                0
[4]      0  44.3117586448  3.3458963222                0
[5]      0  34.6334526911  3.6429838715                0

>>> test_metrics = model.test(frame, ['Dim_1','Dim_2'], 'Class')

>>> test_metrics
accuracy         = 1.0
confusion_matrix =             Predicted_Pos  Predicted_Neg
Actual_Pos              3              0
Actual_Neg              0              3
f_measure        = 1.0
precision        = 1.0
recall           = 1.0

>>> model.save("sandbox/randomforestclassifier")

>>> restored = tc.load("sandbox/randomforestclassifier")

>>> restored.label_column == model.label_column
True

>>> restored.seed == model.seed
True

>>> set(restored.observation_columns) == set(model.observation_columns)
True
```  

The trained model can also be exported to a .mar file, to be used with the scoring engine:  
```  
>>> canonical_path = model.export_to_mar("sandbox/rfClassifier.mar")
```  

<a name="model-revision">
#Model revision support
You can deploy models of the same type (Linear Regression, Random Forest, K-means, etc.) *and* using the same I/O parameters as the original model *without* having to redeploy a scoring engine. This allows you to focus more on analysis and less on process.

##Examples (python):  
Revising a model of the same type and with same I/O parameters: 

* revise model via .mar file 

	```
    files = {'file': open('/path/to/revised_model.mar', 'rb')}
    requests.post(url='http://localhost:9100/forceReviseMarFile', files=files)
    ```

* revise model via byte stream of model file 

	```
	modelBytes = open('./revised_model.mar', 'rb').read()
	requests.post(url='http://localhost:9100/v2/reviseMarBytes', data=modelBytes, headers={'Content-Type': 'application/octet-stream'})
	```

Forcefully revising incompatible model i.e revised model has different input and/or output paramaeters and different model type that existing model in scoring engine:  

* forcefully revise model via .mar file

	```
    files = {'file': open('/path/to/revised_model.mar', 'rb')}
    requests.post(url='http://localhost:9100/v2/forceReviseMarFile', files=files)
    ```

* forcefully revising model via byte stream of model file 

	```
	modelBytes = open('./revised_model.mar', 'rb').read()
	requests.post(url='http://localhost:9100/v2/forceReviseMarBytes', data=modelBytes, headers={'Content-Type': 'application/octet-stream'})
	```


>If a revised model request comes while batch scoring is in process, the entire in-process batch is scored using the existing model. Then the revised model will be installed, and all following requests will use the revised model.  
  
  
>You can see the metadata for the model being used when you view the scoring engine in your browser.
