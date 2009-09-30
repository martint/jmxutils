# Example

    class ManagedObject
    {
        @Managed
        public int getValue()
        {
           ...
        }

        @Managed
        public void setValue(int value)
        {
           ...
        }

        @Managed
        public void operation()
        {
           ...
        }
    }

    ...
    MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    exporter.export("test:name=X", new ManagedObject());

# Known Limitations

* Doesn't handle inheritance properly. I.e., if a method in a parent class is tagged with @Managed and an overriding
  method in the child class is not, the operation or attribute will not be exported.

# License

Licensed under the Apache License, Version 2.0 (the "License")
You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
