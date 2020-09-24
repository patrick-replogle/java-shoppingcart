package com.lambdaschool.shoppingcart.services;

import com.lambdaschool.shoppingcart.exceptions.ResourceFoundException;
import com.lambdaschool.shoppingcart.exceptions.ResourceNotFoundException;
import com.lambdaschool.shoppingcart.models.Cart;
import com.lambdaschool.shoppingcart.models.Role;
import com.lambdaschool.shoppingcart.models.User;
import com.lambdaschool.shoppingcart.models.UserRoles;
import com.lambdaschool.shoppingcart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service(value = "userService")
public class UserServiceImpl
        implements UserService
{
    /**
     * Connects this service to the users repository
     */
    @Autowired
    private UserRepository userrepos;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserAuditing userAuditing;

    @Override
    public List<User> findAll()
    {
        List<User> list = new ArrayList<>();
        /*
         * findAll returns an iterator set.
         * iterate over the iterator set and add each element to an array list.
         */
        userrepos.findAll()
                .iterator()
                .forEachRemaining(list::add);
        return list;
    }

    @Override
    public User findUserById(long id)
    {
        return userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found!"));
    }

    @Override
    public User findByName(String name)
    {
        return userrepos.findByUsername(name);
    }

    @Override
    public List<User> findByNameContaining(String username)
    {
        List<User> list = userrepos.findByUsernameContainingIgnoreCase(username);
        return list;
    }
    

    @Transactional
    @Override
    public void delete(long id)
    {
        userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found!"));
        userrepos.deleteById(id);
    }

    @Transactional
    @Override
    public User save(User user)
    {
        User newUser = new User();

        newUser.setUsername(user.getUsername());
        newUser.setNoEncodePassword(user.getPassword());
        newUser.setComments(user.getComments());

        newUser.getRoles().clear();

        for (UserRoles ur : user.getRoles())
        {
            Role addRole = roleService.findRoleById(ur.getRole().getRoleid());
            newUser.getRoles().add(new UserRoles(newUser, addRole));
        }

        if (user.getCarts()
                .size() > 0)
        {
            throw new ResourceFoundException("Carts are not added through users");
        }

        return userrepos.save(newUser);
    }

    @Override
    public User update(User user, long id)
    {
        User updateUser = findUserById(id);

        if (user.getUsername() != null)
        {
            updateUser.setUsername(user.getUsername().toLowerCase());
        }

        if (user.getPassword() != null)
        {
            updateUser.setNoEncodePassword(user.getPassword());
        }

        if (user.getComments() != null)
        {
            updateUser.setComments(user.getComments());
        }

        if (user.getRoles().size() > 0)
        {
            updateUser.getRoles()
                    .clear();
            for (UserRoles ur : user.getRoles())
            {
                Role addRole = roleService.findRoleById(ur.getRole()
                        .getRoleid());

                updateUser.getRoles()
                        .add(new UserRoles(updateUser, addRole));
            }
        }
        return userrepos.save(updateUser);
    }
}
