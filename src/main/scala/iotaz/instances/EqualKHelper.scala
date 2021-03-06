/*
 * Copyright 2018 Daniel Spiewak
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

package iotaz
package instances

import scalaz.Equal
import TListK.:::

sealed trait EqualKHelper[LL <: TListK, A] {
  def materialize: Equal[CopK[LL, A]]
}

object EqualKHelper {

  implicit def base[F[_], A](implicit eql: Equal[F[A]]): EqualKHelper[F ::: TNilK, A] = new EqualKHelper[F ::: TNilK, A] {
    type CP[B] = CopK[F ::: TNilK, B]

    def materialize: Equal[CP[A]] = {
      val FA = CopK.Inject[F, CP]

      Equal equalBy {
        case FA(fa) => fa
      }
    }
  }

  implicit def induct[F[_], A, LL <: TListK](implicit eql: Equal[F[A]], LL: EqualKHelper[LL, A]): EqualKHelper[F ::: LL, A] = new EqualKHelper[F ::: LL, A] {
    type CP[B] = CopK[F ::: LL, B]

    def materialize: Equal[CP[A]] = {
      val FA = CopK.Inject.injectFromInjectL[F, F ::: LL](
        CopK.InjectL.makeInjectL[F, F ::: LL](new TListK.Pos[F ::: LL, F] { val index = 0 }))

      Equal equal {
        case (FA(left), FA(right)) => eql.equal(left, right)
        case (left, right) => LL.materialize.equal(left.asInstanceOf[CopK[LL, A]], right.asInstanceOf[CopK[LL, A]])
      }
    }
  }
}
