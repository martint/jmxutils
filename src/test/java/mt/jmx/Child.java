/**
 *  Copyright 2009 Martin Traverso
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package mt.jmx;

public class Child
    extends Parent
    implements Interface2
{
    private int value2;
    private int value3;
    private int covariant;
    private int covariant1;

    public int getValue2()
    {
        return value2;
    }

    public void setValue2(int value2)
    {
        this.value2 = value2;
    }

    public int getValue3()
    {
        return value3;
    }

    public void setValue3(int value3)
    {
        this.value3 = value3;
    }

    public void setCovariant(int covariant)
    {
        this.covariant = covariant;
    }

    @Override
    @Managed
    public Integer getCovariant()
    {
        return covariant;
    }

    @Override
    public Integer getCovariant1()
    {
        return covariant1;
    }

    public void setCovariant1(int covariant1)
    {
        this.covariant1 = covariant1;
    }
}
