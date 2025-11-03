/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.auth.repositories;

import com.owlplug.auth.model.UserAccount;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Spring Data JPA repository for managing {@link UserAccount} entities.
 * This interface provides standard CRUD (Create, Read, Update, Delete) operations
 * for user accounts, along with custom query methods to manage account data.
 */
public interface UserAccountRepository extends CrudRepository<UserAccount, Long> {

    /**
     * Deletes user accounts that are considered invalid, specifically those
     * where the {@code accountProvider} is {@code NULL}. This operation is
     * transactional and will clear and flush the persistence context automatically
     * after execution.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserAccount u WHERE u.accountProvider = NULL")
    void deleteInvalidAccounts();

}
