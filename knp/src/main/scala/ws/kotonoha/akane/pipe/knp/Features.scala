/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.analyzers.knp.FeatureAccess

/**
 * @author eiennohito
 * @since 15/07/14
 */
object Features {

  val yougen: String = "用言"
  val prefix: String = "接頭"
  val predArg = "格解析結果"
  val normalizedWriting = "正規化代表表記"

  def isYougen(api: FeatureAccess): Boolean = api.featureExists(yougen)

  def normWriting(floc: FeatureAccess) = {
    floc.valueOfFeature(normalizedWriting)
  }
}
