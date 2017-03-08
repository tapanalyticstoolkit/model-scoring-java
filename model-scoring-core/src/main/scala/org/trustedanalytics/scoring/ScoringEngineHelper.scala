/**
  *  Copyright (c) 2015 Intel Corporation
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */

package org.trustedanalytics.scoring

import java.io.File
import java.util.{ArrayList => JArrayList}

import org.slf4j.LoggerFactory
import org.trustedanalytics.model.archive.format.ModelArchiveFormat
import org.trustedanalytics.scoring.interfaces.Model

object ScoringEngineHelper {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
    *
    * @param model        Original model
    * @param revisedModel Revised model
    * @return true if model compatible i.e model type is same and model input/output parameters are same
    *         else returns false
    */
  def isModelCompatible(model: Model, revisedModel: Model): Boolean = {
    model.modelMetadata().modelType == revisedModel.modelMetadata().modelType &&
      model.input().deep == revisedModel.input().deep &&
      model.output().deep == revisedModel.output().deep
  }

  def getModel(marFilePath: String): Model = {
    if (marFilePath != "") {
      logger.info("calling ModelArchiveFormat to get the model")
      ModelArchiveFormat.read(new File(marFilePath), this.getClass.getClassLoader, None)
    }
    else {
      null
    }
  }
}