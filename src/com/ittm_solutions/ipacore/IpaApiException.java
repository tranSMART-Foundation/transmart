/*
 * Copyright (C) 2016 ITTM S.A.
 *
 * Written by Nils Christian <nils.christian@ittm-solutions.com>.
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
 *
 */

package com.ittm_solutions.ipacore;

public class IpaApiException extends Exception {
    private static final long serialVersionUID = 1L;

    public IpaApiException() {
    }

    public IpaApiException(String message) {
        super(message);
    }

    public IpaApiException(Throwable throwable) {
        super(throwable);
    }

    public IpaApiException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
