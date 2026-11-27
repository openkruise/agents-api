# Copyright 2026 The Kruise Authors.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


class TemplateError(Exception):
    """Base error for the custom API, independent of E2B's Template protocol."""


class TemplateAPIError(TemplateError):
    def __init__(self, status_code: int, message: str, request_id=None):
        super().__init__(f"{status_code}: {message}")
        self.status_code = status_code
        self.message = message
        self.request_id = request_id


class TemplateProtocolError(TemplateError):
    """A successful HTTP response does not match the expected custom contract."""


class TemplateStateError(TemplateError):
    def __init__(self, name: str, status: str, message=None):
        super().__init__(f"Template {name!r} is {status}: {message or ''}")
        self.name = name
        self.status = status
        self.message = message


class TemplateWaitTimeout(TemplateError, TimeoutError):
    """Waiting expired. Resources are neither cancelled nor deleted."""
