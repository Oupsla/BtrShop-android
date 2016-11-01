package fr.lille1.univ.android.architecture;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import fr.lille1.univ.android.architecture.btrshop.UseCaseHandler;
import fr.lille1.univ.android.architecture.btrshop.data.source.ProductsRepository;
import fr.lille1.univ.android.architecture.btrshop.products.domain.usecase.GetProduct;

/**
 * Enables injection of mock implementations for
 * at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static ProductsRepository provideProductsRepository(@NonNull Context context) {
        checkNotNull(context);
        return ProductsRepository.getInstance();
    }

    public static UseCaseHandler provideUseCaseHandler() {
        return UseCaseHandler.getInstance();
    }


    public static GetProduct provideGetProduct(@NonNull Context context) {
        return new GetProduct(Injection.provideProductsRepository(context));
    }



}
